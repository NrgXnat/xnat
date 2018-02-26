/*
 * web: org.nrg.xnat.helpers.prearchive.SessionXMLRebuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.task.XnatTask;
import org.nrg.framework.task.services.XnatTaskService;
import org.nrg.xdat.XDAT;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.nrg.xnat.task.AbstractXnatTask;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;

import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

/**
 * The Class SessionXMLRebuilder.
 */
@XnatTask(taskId = "SessionXMLRebuilder", description = "Session XML Rebuilder", defaultExecutionResolver = "SingleNodeExecutionResolver", executionResolverConfigurable = true)
@Slf4j
public class SessionXMLRebuilder extends AbstractXnatTask {
    /**
     * Instantiates a new session XML rebuilder.
     *
     * @param provider    the provider
     * @param appInfo     the app info
     * @param jmsTemplate the jms template
     * @param interval    the interval
     */
    public SessionXMLRebuilder(final Provider<UserI> provider, final XnatTaskService taskService, final XnatAppInfo appInfo, final JmsTemplate jmsTemplate, final JdbcTemplate jdbcTemplate, final double interval) {
        super(taskService, true, appInfo, jdbcTemplate);
        _provider = provider;
        _interval = interval;
        _jmsTemplate = jmsTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runTask() {
        try {
            final UserI user = _provider.get();
            if (user == null) {
                log.warn("The user for running the session XML rebuilder process was not found. Aborting for now.");
                return;
            }

            if (!PrearcDatabase.ready) {
                log.info("The prearchive database is not ready, exiting.");
                return;
            }

            log.trace("Running prearc job as {}", user.getLogin());
            final List<SessionData> allSessions = PrearcDatabase.getAllSessions();

            int updated = 0;
            int total   = 0;
            log.info("Checking whether any prearc entries should be processed, found {} sessions", allSessions.size());
            if (!allSessions.isEmpty()) {
                final long now = Calendar.getInstance().getTimeInMillis();
                for (final SessionData sessionData : allSessions) {
                    total++;
                    if (sessionData.getStatus().equals(PrearcUtils.PrearcStatus.RECEIVING) && !sessionData.getPreventAutoCommit() && !StringUtils.trimToEmpty(sessionData.getSource()).equals(SessionData.UPLOADER)) {
                        try {
                            final File   sessionDir = PrearcUtils.getPrearcSessionDir(user, sessionData.getProject(), sessionData.getTimestamp(), sessionData.getFolderName(), false);
                            final long   then       = sessionData.getLastBuiltDate().getTime();
                            final double diff       = diffInMinutes(then, now);
                            log.debug("Prearchive session {} is {} minutes old", sessionData.toString(), diff);
                            if (diff >= _interval && !PrearcUtils.isSessionReceiving(sessionData.getSessionDataTriple())) {
                                updated++;
                                try {
                                    log.info("Prearchive session {} is {} minutes old, greater than configured interval {}, setting status to QUEUED_BUILDING", sessionData.toString(), diff, _interval);
                                    if (PrearcDatabase.setStatus(sessionData.getFolderName(), sessionData.getTimestamp(), sessionData.getProject(), PrearcUtils.PrearcStatus.QUEUED_BUILDING)) {
                                        log.debug("Creating JMS queue entry for {} to archive {}", user.getUsername(), sessionData.getExternalUrl());
                                        final PrearchiveOperationRequest request = new PrearchiveOperationRequest(user, sessionData, sessionDir, "Rebuild");
                                        XDAT.sendJmsRequest(_jmsTemplate, request);
                                    }
                                } catch (Exception exception) {
                                    log.error("Error when setting prearchive session status to QUEUED", exception);
                                }
                            } else if (diff >= (_interval * 10)) {
                                log.error(String.format("Prearchive session locked for an abnormally large time within CACHE_DIR/prearc_locks/%1$s/%2$s/%3$s", sessionData.getProject(), sessionData.getTimestamp(), sessionData.getName()));
                            } else if (diff < _interval) {
                                log.debug("Prearchive session {} is {} minutes old, less than configured interval {}, remaining in RECEIVING status", sessionData.toString(), diff, _interval);
                            }
                        } catch (IOException e) {
                            final String message = String.format("An error occurred trying to write the session %s %s %s.", sessionData.getFolderName(), sessionData.getTimestamp(), sessionData.getProject());
                            log.error(message, e);
                        } catch (InvalidPermissionException e) {
                            final String message = String.format("A permissions error occurred trying to write the session %s %s %s.", sessionData.getFolderName(), sessionData.getTimestamp(), sessionData.getProject());
                            log.error(message, e);
                        } catch (Exception e) {
                            final String message = String.format("An unknown error occurred trying to write the session %s %s %s.", sessionData.getFolderName(), sessionData.getTimestamp(), sessionData.getProject());
                            log.error(message, e);
                        }
                    }
                }
            }
            log.info("Built {} of {}", updated, total);
        } catch (SessionException e) {
            log.error("", e);
        } catch (SQLException e) {
            // Swallow this message so it doesn't fill the logs before the prearchive is initialized.
            if (!e.getMessage().contains("relation \"xdat_search.prearchive\" does not exist")) {
                log.error("", e);
            }
        } catch (final NrgServiceRuntimeException e) {
            switch (e.getServiceError()) {
                case UserServiceError:
                    log.warn("The user for running the session XML rebuilder process could not be initialized. This probably means the system is still initializing. Check the database if this is not the case.");
                    break;
                default:
                    throw e;
            }
        } catch (final Exception e) {
            log.error("An unknown error occurred", e);
        }
    }

    /**
     * Diff in minutes.
     *
     * @param start the start
     * @param end   the end
     *
     * @return the double
     */
    public static double diffInMinutes(long start, long end) {
        double seconds = Math.floor((end - start) / 1000);
        return Math.floor(seconds / 60);
    }

    private final Provider<UserI> _provider;
    private final double          _interval;
    private final JmsTemplate     _jmsTemplate;
}
