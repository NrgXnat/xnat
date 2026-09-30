/*
 * web: org.nrg.xnat.turbine.utils.ArcSpecManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.utils;

import static org.nrg.xnat.turbine.utils.XNATUtils.setArcProjectPaths;

import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.ArcProjectI;
import org.nrg.xdat.om.ArcArchivespecification;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.preferences.SmtpServer;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.restlet.util.DateUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author timo
 */
@Slf4j
public class ArcSpecManager {
    public static Date lastModified=null;
    public static Long lastChecked= Calendar.getInstance().getTimeInMillis();
    public static Long lastCheckedInterval =  60000L;

    private static ArcArchivespecification arcSpec = null;

    public synchronized static ArcArchivespecification GetFreshInstance() {
        log.info("Getting fresh ArcSpec...");
		final List<ArcArchivespecification> allSpecs = ArcArchivespecification.getAllArcArchivespecifications(null, false);
	    return allSpecs.isEmpty() ? null : allSpecs.getFirst();
	}
    
    public synchronized static ArcArchivespecification GetInstance() {
    	return GetInstance(true);
    }
    
    public synchronized static  ArcArchivespecification GetInstance(boolean dbInit){
        Date currentModDate=null;
        try {
            if((lastChecked+lastCheckedInterval<Calendar.getInstance().getTimeInMillis())) {
                //rapid rechecking of this is expenseive and unnecessary.  Sometimes dozens of times per request.  Allow it to be cached for a bit.
                currentModDate = (Date) PoolDBUtils.ReturnStatisticQuery("SELECT MAX(last_modified) AS last_modified FROM arc_archivespecification_meta_data", "last_modified", null, null);
                lastChecked=Calendar.getInstance().getTimeInMillis();
            }
        } catch (SQLException e1) {
            log.error("",e1);
        } catch (Exception e1) {
            log.error("",e1);
        }

        if (arcSpec == null || (lastModified != null && currentModDate != null && DateUtils.before(lastModified, currentModDate))) {
            lastModified = currentModDate;
            log.info("Initializing ArcSpec...");
            arcSpec = GetFreshInstance();

            if (arcSpec != null) {
                // In the background: writing the file loads every project, and this lock holds up every lookup.
                xmlRewrites.request();
            }

            if(dbInit) {
	            try {
                    PrearcDatabase.initDatabase(XDAT.getBoolSiteConfigurationProperty("reloadPrearcDatabaseOnStartup", false));
	    		} catch (Exception e) {
	    			log.error("", e);
	    		}
            }
        }
        
        return arcSpec;
    }

    private static boolean _isComplete=false;
    private static Object _lock = new Object();
    public static boolean isComplete(){
        if(!_isComplete){
            synchronized (_lock){
                if(!_isComplete){
                    ArcArchivespecification spec = ArcSpecManager.GetInstance();
                    if(spec!=null && spec.isComplete()){
                        _isComplete=true;
                    }
                }
            }
        }
        return _isComplete;
    }

    public synchronized static  void Reset(){
        arcSpec=null;
    }

    /**
     * Records that the project's arc settings were saved. Unlike {@link #Reset()}, the cached specification is kept:
     * discarding it made the next lookup load every project, one query each, after every project was created. This
     * project's entry is dropped, so an arc project saved again is loaded whatever its last-modified time. A new
     * project and the other projects are loaded as on every lookup, by their last-modified times.
     * {@code archive_specification.xml} is rewritten in the background, at most once every
     * {@value #XML_REWRITE_DELAY_SECONDS} seconds however many projects change.
     */
    public static void arcProjectSaved(final String projectId) {
        final ArcArchivespecification spec = cachedInstance();
        if (spec != null) {
            spec.evictArcProject(projectId);
        }
        xmlRewrites.request();
    }

    /** The specification as cached, without loading one: taken under the lock, used outside it. */
    private synchronized static ArcArchivespecification cachedInstance() {
        return arcSpec;
    }

    public synchronized static ArcArchivespecification initialize(final UserI user) throws Exception {
        arcSpec = new ArcArchivespecification(user);
        final SiteConfigPreferences siteConfigPreferences = XDAT.getSiteConfigPreferences();
        final NotificationsPreferences notificationsPreferences = XDAT.getNotificationsPreferences();
        if (StringUtils.isNotBlank(siteConfigPreferences.getAdminEmail())) {
            log.info("Setting site admin email to: {}", siteConfigPreferences.getAdminEmail());
            arcSpec.setSiteAdminEmail(siteConfigPreferences.getAdminEmail());
        }

        if (StringUtils.isNotBlank(siteConfigPreferences.getSiteId())) {
            log.info("Setting site ID to: {}", siteConfigPreferences.getSiteId());
            arcSpec.setSiteId(siteConfigPreferences.getSiteId());
        }

        if (StringUtils.isNotBlank(siteConfigPreferences.getSiteUrl())) {
            log.info("Setting site URL to: {}", siteConfigPreferences.getSiteUrl());
            arcSpec.setSiteUrl(siteConfigPreferences.getSiteUrl());
        }

        final SmtpServer smtpServer = notificationsPreferences.getSmtpServer();
        if (smtpServer != null) {
            final String hostname = smtpServer.getHostname();
            log.info("Setting SMTP host to: {}", hostname);
            arcSpec.setSmtpHost(hostname);
        }

        log.info("Setting enable new registrations to: {}", siteConfigPreferences.getUserRegistration());
        arcSpec.setEnableNewRegistrations(siteConfigPreferences.getUserRegistration());

        log.info("Setting require login to: {}", siteConfigPreferences.getRequireLogin());
        arcSpec.setRequireLogin(siteConfigPreferences.getRequireLogin());

        if (StringUtils.isNotBlank(siteConfigPreferences.getPipelinePath())) {
            log.info("Setting pipeline path to: {}", siteConfigPreferences.getPipelinePath());
            arcSpec.setProperty("globalPaths/pipelinePath", siteConfigPreferences.getPipelinePath());
        }

        if (StringUtils.isNotBlank(siteConfigPreferences.getArchivePath())) {
            log.info("Setting archive path to: {}", siteConfigPreferences.getArchivePath());
            arcSpec.setProperty("globalPaths/archivePath", siteConfigPreferences.getArchivePath());
        }

        if (StringUtils.isNotBlank(siteConfigPreferences.getPrearchivePath())) {
            log.info("Setting prearchive path to: {}", siteConfigPreferences.getPrearchivePath());
            arcSpec.setProperty("globalPaths/prearchivePath", siteConfigPreferences.getPrearchivePath());
        }

        if (StringUtils.isNotBlank(siteConfigPreferences.getCachePath())) {
            log.info("Setting cache path to: {}", siteConfigPreferences.getCachePath());
            arcSpec.setProperty("globalPaths/cachePath", siteConfigPreferences.getCachePath());
        }

        if (StringUtils.isNotBlank(siteConfigPreferences.getFtpPath())) {
            log.info("Setting FTP path to: {}", siteConfigPreferences.getFtpPath());
            arcSpec.setProperty("globalPaths/ftpPath", siteConfigPreferences.getFtpPath());
        }

        if (StringUtils.isNotBlank(siteConfigPreferences.getBuildPath())) {
            log.info("Setting build path to: {}", siteConfigPreferences.getBuildPath());
            arcSpec.setProperty("globalPaths/buildPath", siteConfigPreferences.getBuildPath());
        }

        for (final ArcProjectI arcProject : arcSpec.getProjects_project()) {
            setArcProjectPaths(arcProject, siteConfigPreferences);
        }

        log.info("Setting enable CSRF token to: {}", siteConfigPreferences.getEnableCsrfToken());
        arcSpec.setEnableCsrfToken(siteConfigPreferences.getEnableCsrfToken());

        log.info("Saving arcspec");
        save(arcSpec, user, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.PROCESS, "Initialized archive specifications."));
        return arcSpec;
    }

    public static boolean allowTransferEmail() {
        return GetInstance().getEmailspecifications_transfer();
    }
    
    public static synchronized void save(ArcArchivespecification arcSpec, EventDetails event) throws Exception {
        save(arcSpec, arcSpec.getUser(), event);
}

    public static synchronized void save(ArcArchivespecification arcSpec, UserI user, EventDetails event) throws Exception {
        SaveItemHelper.unauthorizedSave(arcSpec, user, false, false, event);
        ArcSpecManager.Reset();
    }

    /**
     * Writes the specification to {@code archive_specification.xml} in the global cache path, if one is set. Only the
     * background rewrite calls this, on its one thread, so two writes never overlap.
     */
    private static void writeXml(final ArcArchivespecification spec) {
        final String cachePath = spec.getGlobalCachePath();
        if (StringUtils.isBlank(cachePath)) {
            return;
        }
        final File arcSpecFile       = new File(cachePath, "archive_specification.xml");
        final File arcSpecFileFolder = arcSpecFile.getParentFile();
        if (!arcSpecFileFolder.exists() && !arcSpecFileFolder.mkdirs()) {
            throw new RuntimeException("Failed to create working file " + arcSpecFile.getAbsolutePath() + ", please check permissions and file system.");
        }
        log.debug("Initializing arcspec to cache file {}", arcSpecFile.getAbsolutePath());
        try (FileWriter writer = new FileWriter(arcSpecFile)) {
            spec.toXML(writer, true);
        } catch (IllegalArgumentException | IOException | SAXException e) {
            log.error("", e);
        }
    }

    /**
     * Rewrites {@code archive_specification.xml} from a specification loaded for the purpose, outside the lock that
     * {@link #GetInstance()} holds, so requests don't wait on it.
     */
    private static void rewriteXml() {
        try {
            final List<ArcArchivespecification> specs = ArcArchivespecification.getAllArcArchivespecifications(null, false);
            if (!specs.isEmpty()) {
                writeXml(specs.getFirst());
            }
        } catch (RuntimeException e) {
            log.error("Unable to rewrite the archive specification XML", e);
        }
    }

    /**
     * Runs a task once, a delay after the first of any number of requests. A request made once the run has started gets
     * another run, so every request is followed by a run that starts after it.
     */
    static final class DeferredRun {
        DeferredRun(final ScheduledExecutorService executor, final Runnable task, final long delay, final TimeUnit unit) {
            _executor = executor;
            _task     = task;
            _delay    = delay;
            _unit     = unit;
        }

        void request() {
            if (_pending.compareAndSet(false, true)) {
                _executor.schedule(() -> {
                    _pending.set(false);
                    _task.run();
                }, _delay, _unit);
            }
        }

        private final AtomicBoolean            _pending = new AtomicBoolean();
        private final ScheduledExecutorService _executor;
        private final Runnable                 _task;
        private final long                     _delay;
        private final TimeUnit                 _unit;
    }

    static final long XML_REWRITE_DELAY_SECONDS = 10;

    // Not final, so tests can put their own in its place.
    static DeferredRun xmlRewrites = new DeferredRun(rewriteExecutor(1, TimeUnit.MINUTES), ArcSpecManager::rewriteXml, XML_REWRITE_DELAY_SECONDS, TimeUnit.SECONDS);

    /**
     * One daemon thread, which exits once it has been idle for the given time and starts again at the next request.
     * Nothing stops it when the web application stops, so it exits on its own instead of holding the application's
     * class loader after a redeploy.
     */
    static ScheduledThreadPoolExecutor rewriteExecutor(final long idle, final TimeUnit unit) {
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, runnable -> {
            final Thread thread = new Thread(runnable, "arc-spec-xml-rewrite");
            thread.setDaemon(true);
            return thread;
        });
        executor.setKeepAliveTime(idle, unit);
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }
}
