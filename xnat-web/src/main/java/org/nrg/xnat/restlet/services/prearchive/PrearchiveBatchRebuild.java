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
import org.nrg.xdat.XDAT;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionDataTriple;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import java.util.List;

import static org.nrg.xnat.archive.Operation.Rebuild;
import static org.nrg.xnat.helpers.prearchive.handlers.PrearchiveRebuildHandler.PARAM_OVERRIDE_LOCK;

@Slf4j
public class PrearchiveBatchRebuild extends BatchPrearchiveActionsA {
	public PrearchiveBatchRebuild(final Context context, final Request request, final Response response) {
		super(context, request, response);
	}

	@Override
	public void handleParam(final String key, final Object value) {
		if (PARAM_OVERRIDE_LOCK.equals(key)) {
			_overrideLock = Boolean.parseBoolean((String) value);
			getAdditionalValues().put(PARAM_OVERRIDE_LOCK, _overrideLock);
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

		final UserI   user         = getUser();
		for (final SessionDataTriple triple : triples) {
			try {
				if (PrearcDatabase.setStatus(triple.getFolderName(), triple.getTimestamp(), triple.getProject(), PrearcUtils.PrearcStatus.QUEUED_BUILDING, _overrideLock)) {
					XDAT.sendJmsRequest(new PrearchiveOperationRequest(user, Rebuild, triple, getAdditionalValues()));
				} else {
					log.warn("Tried to reset the status of the session {} to QUEUED_BUILDING, but failed. This usually means the session is locked and the override lock parameter was false. This might be OK: I checked whether the session was locked before trying to update the status but maybe a new file arrived in the intervening millisecond(s).", triple);
				}
            } catch (IllegalArgumentException e) {
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e);
            } catch (InvalidPermissionException e) {
				getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, e);
            } catch (Exception exception) {
                log.error("Error when setting prearchive session {} status to QUEUED for user {}", triple.toString(), user.getUsername(), exception);
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,exception);
            }
        }

		setTriplesRepresentation(triples);
	}

	private boolean _overrideLock;
}
