/*
 * web: org.nrg.xnat.restlet.services.prearchive.PrearchiveBatchMove
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.services.prearchive;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.XDAT;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.prearchive.SessionDataTriple;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nrg.xnat.archive.Operation.Move;

/**
 * @author tolsen01
 */
@Slf4j
public class PrearchiveBatchMove extends BatchPrearchiveActionsA {
    /**
     * Sets up the move operation class.
     *
     * @param context  The Restlet context.
     * @param request  The Restlet request.
     * @param response The Restlet response.
     */
    public PrearchiveBatchMove(final Context context, final Request request, final Response response) {
        super(context, request, response);
    }

    @Override
    public void handleParam(final String key, final Object value) {
        switch (key) {
            case SRC:
                getSources().add((String) value);
                break;
            case NEW_PROJECT:
                newProject = (String) value;
                break;
            case ASYNC:
                if (value.equals("false")) {
                    setAsync(false);
                }
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
        for (final SessionDataTriple triple : triples) {
            try {
                if (PrearcDatabase.setStatus(triple.getFolderName(), triple.getTimestamp(), triple.getProject(), PrearcUtils.PrearcStatus.QUEUED_MOVING)) {
                    final SessionData session = PrearcDatabase.getSession(triple.getFolderName(), triple.getTimestamp(), triple.getProject());
                    final File sessionDir = PrearcUtils.getPrearcSessionDir(user, triple.getProject(), triple.getTimestamp(), triple.getFolderName(), false);

                    final Map<String, Object> parameters = new HashMap<>();
                    parameters.put(PrearchiveOperationRequest.PARAM_DESTINATION, newProject);

                    XDAT.sendJmsRequest(new PrearchiveOperationRequest(user, Move, session, sessionDir, parameters));
                }
            } catch (Exception e) {
                log.error("", e);
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
                return;
            }
        }

        setTriplesRepresentation(triples);
    }

    private static final String NEW_PROJECT = "newProject";

    private String newProject = null;
}
