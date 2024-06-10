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
				final Map<String, Object> additionalValues = getAdditionalValues();
				additionalValues.put(PARAM_AUTO_ARCHIVE_BLOCKED, true);
				final PrearchiveOperationRequest request = new PrearchiveOperationRequest(user, Rebuild, triple,
						additionalValues);
				PrearcUtils.queuePrearchiveOperation(request);
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
