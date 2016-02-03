/*
 * org.nrg.xnat.restlet.services.prearchive.PrearchiveBatchMove
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 12/19/13 3:01 PM
 */

/**
 *
 */
package org.nrg.xnat.restlet.services.prearchive;

import org.nrg.action.ClientException;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.prearchive.SessionDataTriple;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tolsen01
 */
public class PrearchiveBatchMove extends BatchPrearchiveActionsA {

    private static final String NEW_PROJECT = "newProject";
    private static final Logger logger = LoggerFactory.getLogger(PrearchiveBatchMove.class);

    /**
     * Sets up the move operation class.
     *
     * @param context  The Restlet context.
     * @param request  The Restlet request.
     * @param response The Restlet response.
     */
    public PrearchiveBatchMove(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void handleParam(String key, Object o) throws ClientException {
        switch (key) {
            case SRC:
                srcs.add((String) o);
                break;
            case NEW_PROJECT:
                newProject = (String) o;
                break;
            case ASYNC:
                if (o.equals("false")) {
                    async = false;
                }
                break;
        }
    }

    @Override
    public void handlePost() {

        try {
            loadBodyVariables();
            //maintain parameters
            loadQueryVariables();
        } catch (ClientException e) {
            getResponse().setStatus(e.getStatus(), e);
            return;
        }

        if (newProject == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Move operation requires 'newProject'");
            return;
        } else {
            try {
                if (!PrearcUtils.canModify(user, newProject)) {
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Invalid permissions for new project.");
                    return;
                }
            } catch (Exception e) {
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
                return;
            }
        }

        final List<SessionDataTriple> ss = new ArrayList<>();

        for (final String src : srcs) {
            File sessionDir;
            try {
                SessionDataTriple s = buildSessionDataTriple(src);
                if (!PrearcUtils.canModify(user, s.getProject())) {
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Invalid permissions for new project.");
                    return;
                }
                ss.add(s);
                sessionDir = PrearcUtils.getPrearcSessionDir(user, s.getProject(), s.getTimestamp(), s.getFolderName(), false);

                if (PrearcDatabase.setStatus(s.getFolderName(), s.getTimestamp(), s.getProject(), PrearcUtils.PrearcStatus.QUEUED_MOVING)) {
                    SessionData session = PrearcDatabase.getSession(s.getFolderName(), s.getTimestamp(), s.getProject());

                    final Map<String, String> parameters = new HashMap<>();
                    parameters.put(PrearchiveOperationRequest.PARAM_DESTINATION, newProject);

                    XDAT.sendJmsRequest(new PrearchiveOperationRequest(user, session, sessionDir, "Move", parameters));
                }
            } catch (Exception e) {
                logger.error("", e);
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
                return;
            }
        }

        final Response response = getResponse();
        try {
            response.setEntity(updatedStatusRepresentation(ss, overrideVariant(getPreferredVariant())));
        } catch (Exception e) {
            logger.error("", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
        }
    }

    private String newProject = null;
}
