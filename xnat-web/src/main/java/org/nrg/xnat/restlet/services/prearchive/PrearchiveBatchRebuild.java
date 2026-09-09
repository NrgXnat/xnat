/*
 * web: org.nrg.xnat.restlet.services.prearchive.PrearchiveBatchRebuild
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.services.prearchive;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.prearchive.PrearcBatchRecovery;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcLockRecovery;
import org.nrg.xnat.helpers.prearchive.PrearcRecoveryAction;
import org.nrg.xnat.helpers.prearchive.PrearcRecoveryOutcome;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.prearchive.SessionDataTriple;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nrg.xnat.archive.Operation.Rebuild;
import static org.nrg.xnat.helpers.prearchive.handlers.PrearchiveRebuildHandler.PARAM_AUTO_ARCHIVE_BLOCKED;
import static org.nrg.xnat.helpers.prearchive.handlers.PrearchiveRebuildHandler.PARAM_OVERRIDE_LOCK;

@Slf4j
public class PrearchiveBatchRebuild extends BatchPrearchiveActionsA {
	public PrearchiveBatchRebuild(final Context context, final Request request, final Response response) {
		super(context, request, response);
	}

	@Override
	public void handleParam(final String key, final Object value) {
		if (PARAM_OVERRIDE_LOCK.equals(key)) {
			getAdditionalValues().put(PARAM_OVERRIDE_LOCK, Boolean.parseBoolean((String) value));
		} else {
			super.handleParam(key, value);
		}
	}

	@Override
	public void handlePost() {
		if (!loadVariables()) {
			return;
		}

		final List<SessionDataTriple> triples = getSessionDataTriples();
		if (triples == null) {
			return;
		}

		final UserI user = getUser();

		final List<PrearcRecoveryOutcome> outcomes = PrearcBatchRecovery.run(triples, new RestletRecoveryOps(user), Roles.isSiteAdmin(user));

		// Only the sessions actually acted on are reported, and the response is written whatever happened, so a caller
		// can always tell which of the sessions they selected were dealt with.
		final List<SessionDataTriple> affected = new ArrayList<>();
		for (final PrearcRecoveryOutcome outcome : outcomes) {
			if (outcome.succeeded()) {
				affected.add(outcome.triple());
			} else {
				reportFirstFailure(outcome);
			}
		}
		setTriplesRepresentation(affected);
	}

	/**
	 * Sets the response status from the first session that didn't work out. The first one wins because it is the one
	 * that explains why the batch is incomplete, rather than whichever session happened to be last.
	 */
	private void reportFirstFailure(final PrearcRecoveryOutcome outcome) {
		if (_failed) {
			return;
		}
		_failed = true;

		final Exception error = outcome.error();
		if (error instanceof InvalidPermissionException) {
			getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, error.getMessage());
		} else if (error instanceof IllegalArgumentException) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, error.getMessage());
		} else if (error != null) {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, error.getMessage());
		} else if (outcome.action() == PrearcRecoveryAction.LOCKED_REQUIRES_ADMIN) {
			// A permission problem, not a conflict: the session's state is fine, this user just may not touch it.
			getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Prearchive session " + outcome.triple() + " is locked. Only a site administrator can clear that lock.");
		} else {
			getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Prearchive session " + outcome.triple() + " could not be queued for rebuild; another operation holds it.");
		}
	}

	/** Connects {@link PrearcBatchRecovery} to the prearchive database and the operation queue. */
	private class RestletRecoveryOps implements PrearcBatchRecovery.RecoveryOps {
		private final UserI _user;

		RestletRecoveryOps(final UserI user) {
			_user = user;
		}

		@Override
		public SessionData load(final SessionDataTriple triple) throws Exception {
			return PrearcDatabase.getSession(triple);
		}

		@Override
		public boolean queueRebuild(final SessionDataTriple triple, final SessionData sessionData, final boolean force) throws Exception {
			// Each session needs its own copy: whether we override the lock is decided per session, so sharing one map
			// would leak one session's decision onto the next.
			final Map<String, Object> additionalValues = new HashMap<>(getAdditionalValues());
			additionalValues.put(PARAM_AUTO_ARCHIVE_BLOCKED, true);
			if (force) {
				// Logged at WARN deliberately: overriding a session lock is an exceptional action and this line is the
				// audit trail for it. The org.nrg.xnat.restlet logger ships at WARN, so INFO would be invisible.
				log.warn("Site administrator {} is forcing a rebuild of prearchive session {}, which is stranded in the {} state.", _user.getUsername(), triple, sessionData.getStatus());
				// The session log gets the same record as the unlock-to-ERROR path, so the forced rebuild is visible
				// in the session's own history, not just the server log.
				PrearcUtils.log(sessionData, "Session was stranded in the " + sessionData.getStatus() + " state and was force-rebuilt by " + _user.getUsername() + ".");
				// The handler re-checks the lock when it picks the request up, so it needs to know to override it too.
				// Otherwise this is left as the caller sent it: PrearchiveOperationRequest also reads this parameter as
				// getPrearcSessionDir's allowUnassigned flag, so overwriting it would break rebuilds of unassigned
				// sessions for non-administrators.
				additionalValues.put(PARAM_OVERRIDE_LOCK, true);
			}
			return PrearcUtils.queuePrearchiveOperation(new PrearchiveOperationRequest(_user, Rebuild, triple, additionalValues), force);
		}

		/**
		 * Clears a lock left behind by an operation that may already have moved or deleted files. The session is left
		 * in ERROR for an administrator to review rather than rebuilt, because rebuilding it could quietly produce a
		 * session missing whatever the interrupted operation had already moved. See {@link PrearcLockRecovery}.
		 */
		@Override
		public void unlockToError(final SessionDataTriple triple, final SessionData sessionData) throws Exception {
			final PrearcStatus status = sessionData.getStatus();
			log.warn("Site administrator {} asked to rebuild prearchive session {}, which is stranded in the {} state. That operation may already have modified the session on disk, so it is being unlocked to ERROR for review instead of rebuilt.", _user.getUsername(), triple, status);
			PrearcDatabase.setStatus(sessionData, PrearcStatus.ERROR, true);
			PrearcUtils.log(sessionData, "Session was stranded in the " + status + " state and was unlocked to ERROR by " + _user.getUsername() + " rather than rebuilt, because the interrupted operation may have left the session incomplete on disk. Review it before archiving or rebuilding it.");
		}
	}

	private boolean _failed;
}
