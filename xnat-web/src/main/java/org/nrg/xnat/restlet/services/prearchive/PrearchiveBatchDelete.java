/*
 * web: org.nrg.xnat.restlet.services.prearchive.PrearchiveBatchDelete
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
import java.util.List;

import static org.nrg.xnat.archive.Operation.Delete;

/**
 * @author tolsen01
 */
@Slf4j
public class PrearchiveBatchDelete extends BatchPrearchiveActionsA {
    public PrearchiveBatchDelete(Context context, Request request, Response response) {
        super(context, request, response);
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
                if (PrearcDatabase.setStatus(triple.getFolderName(), triple.getTimestamp(), triple.getProject(), PrearcUtils.PrearcStatus.QUEUED_DELETING)) {
                    final SessionData session    = PrearcDatabase.getSession(triple.getFolderName(), triple.getTimestamp(), triple.getProject());
                    final File        sessionDir = PrearcUtils.getPrearcSessionDir(user, triple.getProject(), triple.getTimestamp(), triple.getFolderName(), false);
                    XDAT.sendJmsRequest(new PrearchiveOperationRequest(user, Delete, session, sessionDir));
                }
            } catch (Exception e) {
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
                return;
            }
        }

        setTriplesRepresentation(triples);
    }
}
