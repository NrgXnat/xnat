/*
 * web: org.nrg.xnat.services.messaging.prearchive.DeleteSessionRequestListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.messaging.prearchive;
import java.io.File;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.prearchive.SessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteSessionRequestListener {

    @SuppressWarnings("unused")
    public void onRequest(final DeleteSessionRequest deleteSessionRequest) throws Exception {
        try {
            SessionData sessionData = deleteSessionRequest.getSessionData();
            File sessionDir = deleteSessionRequest.getSessionDir();
            log.info("Received request to process prearchive session at: {}", sessionData.getExternalUrl());
            try {
                if (!sessionDir.getParentFile().exists()) {
                    PrearcDatabase.unsafeSetStatus(sessionData.getFolderName(), sessionData.getTimestamp(), sessionData.getProject(), PrearcUtils.PrearcStatus._DELETING);
                    PrearcDatabase.deleteCacheRow(sessionData.getFolderName(), sessionData.getTimestamp(), sessionData.getProject());
                }
                PrearcDatabase.deleteSession(sessionData.getFolderName(), sessionData.getTimestamp(), sessionData.getProject());
            } catch (PrearcDatabase.SyncFailedException e) {
                log.error("", e);
            } catch (SQLException e) {
                log.error("", e);
            } catch (SessionException e) {
                log.error("", e);
            } catch (Exception e) {
                log.error("", e);
            }
            log.info("Listener completed session delete request.");
        } catch (final Exception exception) {
            // If errors are not logged before they're rethrown, they do not show up in any of the files
            log.error("Choked on request " + deleteSessionRequest + " with the indicated error", exception);
            throw exception;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DeleteSessionRequestListener.class);
}
