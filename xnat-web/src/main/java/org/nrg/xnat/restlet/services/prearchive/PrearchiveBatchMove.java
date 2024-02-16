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
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.prearchive.*;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nrg.xnat.archive.Operation.Move;
import static org.restlet.data.Status.*;

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

                    PrearcUtils.queuePrearchiveOperation(new PrearchiveOperationRequest(user, Move, session, sessionDir, parameters));
                }
            } catch (SessionException e) {
                switch (e.getError()) {
                    case AlreadyExists:
                        getResponse().setStatus(CLIENT_ERROR_CONFLICT, "A prearchive resource with session " + triple.getFolderName() + " and timestamp " + triple.getTimestamp() + " already exists in the project " + triple.getProject());
                        break;

                    case DoesntExist:
                        getResponse().setStatus(CLIENT_ERROR_NOT_FOUND, "No prearchive resource with session " + triple.getFolderName() + " and timestamp " + triple.getTimestamp() + " exists in the project " + triple.getProject());
                        break;

                    case NoProjectSpecified:
                        getResponse().setStatus(CLIENT_ERROR_BAD_REQUEST, "No project specified to move session " + triple.getFolderName() + " and timestamp " + triple.getTimestamp());
                        break;

                    case InvalidStatus:
                        getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, "Can't move session " + triple.getFolderName() + " and timestamp " + triple.getTimestamp() + " in project " + triple.getProject() + " as it has an invalid status");
                        break;

                    case InvalidSession:
                        getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, "The session " + triple.getFolderName() + " and timestamp " + triple.getTimestamp() + " in project " + triple.getProject() + " is invalid (it's not missing, but something's wrong with it)");
                        break;

                    case DatabaseError:
                        getResponse().setStatus(SERVER_ERROR_INTERNAL, e, "A database error occurred trying to move the session " + triple.getFolderName() + " and timestamp " + triple.getTimestamp() + " in project " + triple.getProject());
                        break;
                }
                return;
            } catch (Exception e) {
                log.error("", e);
                getResponse().setStatus(SERVER_ERROR_INTERNAL, e);
                return;
            }
        }

        setTriplesRepresentation(triples);
    }

    private static final String NEW_PROJECT = "newProject";

    private String newProject = null;
}
