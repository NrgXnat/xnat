/*
 * web: org.nrg.xnat.helpers.prearchive.SessionXMLRebuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.task.XnatTask;
import org.nrg.framework.task.services.XnatTaskService;
import org.nrg.xdat.XDAT;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.nrg.xnat.task.AbstractXnatTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class SessionXMLRebuilder extends AbstractXnatTask implements Runnable {

    /**
     * Instantiates a new session xml rebuilder.
     *
     * @param provider the provider
     * @param appInfo the app info
     * @param jmsTemplate the jms template
     * @param interval the interval
     */
    public SessionXMLRebuilder(final Provider<UserI> provider, final XnatAppInfo appInfo, final JmsTemplate jmsTemplate, final JdbcTemplate jdbcTemplate, final double interval) {
        super(XDAT.getContextService().getBean(XnatTaskService.class));
        _provider = provider;
        _appInfo = appInfo;
        _interval = interval;
        _jmsTemplate = jmsTemplate;
        _helper = new DatabaseHelper(jdbcTemplate);
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            if (!_appInfo.isInitialized() || !_helper.tableExists("xdat_search", "prearchive") || !XFTManager.isInitialized()) {
                if (!_markedUninitialized) {
                    logger.warn("Application is not yet initialized, session XML rebuild operation delayed until initialization completed.");
                    _markedUninitialized = true;
                }
                return;
            }
        } catch (final SQLException e) {
            logger.error("An error occurred trying to access the database to check for the table 'xdat_search.prearchive'.", e);
            return;
        }

        try {
            final UserI user = _provider.get();
            if (user == null) {
                logger.warn("The user for running the session XML rebuilder process was not found. Aborting for now.");
                return;
            }
            
            if (!shouldRunTask()) {
            	logger.trace("Session XML rebuilder not configured to run on this node.  Skipping.");
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
                    if (sessionData.getStatus().equals(PrearcUtils.PrearcStatus.RECEIVING) && !sessionData.getPreventAutoCommit() && !StringUtils.trimToEmpty(sessionData.getSource()).equals(SessionData.UPLOADER)) {
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

    /**
     * Diff in minutes.
     *
     * @param start the start
     * @param end the end
     * @return the double
     */
    public static double diffInMinutes(long start, long end) {
        double seconds = Math.floor((end - start) / 1000);
        return Math.floor(seconds / 60);
    }

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(SessionXMLRebuilder.class);

    /** The _provider. */
    private final Provider<UserI> _provider;
    
    /** The _app info. */
    private       XnatAppInfo     _appInfo;
    
    /** The _interval. */
    private final double          _interval;
    
    /** The _jms template. */
    private final JmsTemplate     _jmsTemplate;
    
    /** The helper. */
    private final DatabaseHelper  _helper;

    /** The _marked uninitialized. */
    private boolean _markedUninitialized = false;

}
