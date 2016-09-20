/*
 * web: org.nrg.xnat.helpers.prearchive.SessionXMLRebuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.XDAT;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

public class SessionXMLRebuilder implements Runnable {
    public SessionXMLRebuilder(final Provider<UserI> provider, final XnatAppInfo appInfo, final JmsTemplate jmsTemplate, final double interval) {
        _provider = provider;
        _appInfo = appInfo;
        _interval = interval;
        _jmsTemplate = jmsTemplate;
    }

    @Override
    public void run() {
        if (!_appInfo.isInitialized()) {
            if (!_markedUninitialized) {
                logger.warn("Application is not yet initialized, session XML rebuild operation delayed until initialization completed.");
                _markedUninitialized = true;
            }
            return;
        }

        try {
            final UserI user = _provider.get();
            if (user == null) {
                logger.warn("The user for running the session XML rebuilder process was not found. Aborting for now.");
                return;
            }
            logger.trace("Running prearc job as {}", user.getLogin());
            List<SessionData> sds = null;
            long now = Calendar.getInstance().getTimeInMillis();
            try {
                if (PrearcDatabase.ready) {
                    sds = PrearcDatabase.getAllSessions();
                }
            } catch (SessionException e) {
                logger.error("", e);
            } catch (SQLException e) {
                // Swallow this message so it doesn't fill the logs before the prearchive is initialized.
                if (!e.getMessage().contains("relation \"xdat_search.prearchive\" does not exist")) {
                    logger.error("", e);
                }
            } catch (Exception e) {
                logger.error("", e);
            }
            int updated = 0;
            int total = 0;
            if (sds != null && sds.size() > 0) {
                for (final SessionData sessionData : sds) {
                    total++;
                    if (sessionData.getStatus().equals(PrearcUtils.PrearcStatus.RECEIVING) && !sessionData.getPreventAutoCommit() && !StringUtils.trimToEmpty(sessionData.getSource()).equals("applet")) {
                        try {
                            final File sessionDir = PrearcUtils.getPrearcSessionDir(user, sessionData.getProject(), sessionData.getTimestamp(), sessionData.getFolderName(), false);
                            final long then = sessionData.getLastBuiltDate().getTime();
                            final double diff = diffInMinutes(then, now);
                            if (diff >= _interval && !PrearcUtils.isSessionReceiving(sessionData.getSessionDataTriple())) {
                                updated++;
                                try {
                                    if (PrearcDatabase.setStatus(sessionData.getFolderName(), sessionData.getTimestamp(), sessionData.getProject(), PrearcUtils.PrearcStatus.QUEUED_BUILDING)) {
                                        logger.debug("Creating JMS queue entry for {} to archive {}", user.getUsername(), sessionData.getExternalUrl());
                                        final PrearchiveOperationRequest request = new PrearchiveOperationRequest(user, sessionData, sessionDir, "Rebuild");
                                        XDAT.sendJmsRequest(_jmsTemplate, request);
                                    }
                                } catch (Exception exception) {
                                    logger.error("Error when setting prearchive session status to QUEUED", exception);
                                }
                            } else if (diff >= (_interval * 10)) {
                                logger.error(String.format("Prearchive session locked for an abnormally large time within CACHE_DIR/prearc_locks/%1$s/%2$s/%3$s", sessionData.getProject(), sessionData.getTimestamp(), sessionData.getName()));
                            }
                        } catch (IOException e) {
                            final String message = String.format("An error occurred trying to write the session %s %s %s.", sessionData.getFolderName(), sessionData.getTimestamp(), sessionData.getProject());
                            logger.error(message, e);
                        } catch (InvalidPermissionException e) {
                            final String message = String.format("A permissions error occurred trying to write the session %s %s %s.", sessionData.getFolderName(), sessionData.getTimestamp(), sessionData.getProject());
                            logger.error(message, e);
                        } catch (Exception e) {
                            final String message = String.format("An unknown error occurred trying to write the session %s %s %s.", sessionData.getFolderName(), sessionData.getTimestamp(), sessionData.getProject());
                            logger.error(message, e);
                        }
                    }
                }
            }
            logger.info("Built {} of {}", updated, total);
        } catch (final NrgServiceRuntimeException e) {
            switch (e.getServiceError()) {
                case UserServiceError:
                    logger.warn("The user for running the session XML rebuilder process could not be initialized. This probably means the system is still initializing. Check the database if this is not the case.");
                    break;
                default:
                    throw e;
            }
        }
    }

    public static double diffInMinutes(long start, long end) {
        double seconds = Math.floor((end - start) / 1000);
        return Math.floor(seconds / 60);
    }

    private static final Logger logger = LoggerFactory.getLogger(SessionXMLRebuilder.class);

    private final Provider<UserI> _provider;
    private       XnatAppInfo     _appInfo;
    private final double          _interval;
    private final JmsTemplate     _jmsTemplate;

    private boolean _markedUninitialized = false;
}
