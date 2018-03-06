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
            final List<SessionData> allSessions       = PrearcDatabase.getAllSessions();
            final int               totalSessionCount = allSessions.size();

            int updatedSessionCount   = 0;
            int processedSessionCount = 0;
            log.info("Checking whether any prearc entries should be processed, found {} sessions", totalSessionCount);
            if (!allSessions.isEmpty()) {
                final long now = Calendar.getInstance().getTimeInMillis();
                for (final SessionData sessionData : allSessions) {
                    processedSessionCount++;
                    final SessionDataTriple        triple            = sessionData.getSessionDataTriple();
                    final PrearcUtils.PrearcStatus status            = sessionData.getStatus();
                    final Boolean                  preventAutoCommit = sessionData.getPreventAutoCommit();
                    final String                   source            = sessionData.getSource();

                    log.debug("Testing session #{} of {} total, '{}' with status {}, prevent auto commit {}, source {}", processedSessionCount, totalSessionCount, triple, status, preventAutoCommit, source);

                    if (status.equals(PrearcUtils.PrearcStatus.RECEIVING) && !preventAutoCommit && !StringUtils.trimToEmpty(source).equals(SessionData.UPLOADER)) {
                        try {
                            final File   sessionDir = PrearcUtils.getPrearcSessionDir(user, sessionData.getProject(), sessionData.getTimestamp(), sessionData.getFolderName(), false);
                            final long   then       = sessionData.getLastBuiltDate().getTime();
                            final double diff       = diffInMinutes(then, now);

                            log.debug("Prearchive session '{}' is {} minutes old", sessionData.toString(), diff);

                            if (diff >= _interval && !PrearcUtils.isSessionReceiving(triple)) {
                                updatedSessionCount++;
                                try {
                                    log.info("Update #{}: prearchive session {} is {} minutes old, greater than configured interval {}, setting status to QUEUED_BUILDING", updatedSessionCount, sessionData.toString(), diff, _interval);
                                    if (PrearcDatabase.setStatus(sessionData.getFolderName(), sessionData.getTimestamp(), sessionData.getProject(), PrearcUtils.PrearcStatus.QUEUED_BUILDING)) {
                                        log.debug("Creating JMS queue entry for {} to archive {}", user.getUsername(), sessionData.getExternalUrl());
                                        final PrearchiveOperationRequest request = new PrearchiveOperationRequest(user, sessionData, sessionDir, "Rebuild");
                                        XDAT.sendJmsRequest(_jmsTemplate, request);
                                    } else {
                                        log.warn("Tried to reset the status of the session {} to QUEUED_BUILDING, but failed. This usually means the session is locked and the override lock parameter was false. This might be OK: I checked whether the session was locked before trying to update the status but maybe a new file arrived in the intervening millisecond(s).", sessionData.toString());
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
            log.info("Built {} of {}", updatedSessionCount, processedSessionCount);
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
