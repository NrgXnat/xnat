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
import java.util.Date;
import java.util.List;

/**
 * @author timo
 */
@Slf4j
public class ArcSpecManager {
    public static Date lastModified = null;

    private static ArcArchivespecification arcSpec = null;

    public synchronized static ArcArchivespecification GetFreshInstance() {
        log.info("Getting fresh ArcSpec...");
		final List<ArcArchivespecification> allSpecs = ArcArchivespecification.getAllArcArchivespecifications(null, false);
	    return allSpecs.isEmpty() ? null : allSpecs.get(0);
	}
    
    public synchronized static ArcArchivespecification GetInstance() {
    	return GetInstance(true);
    }

    public synchronized static ArcArchivespecification GetInstance(final boolean dbInit) {
        final Date currentModDate;
        try {
            currentModDate = (Date) PoolDBUtils.ReturnStatisticQuery("SELECT MAX(last_modified) AS last_modified FROM arc_archivespecification_meta_data", "last_modified", null, null);
        } catch (Exception e) {
            log.error("An error occurred trying to query the last modified date for the ArcSpec", e);
            return arcSpec;
        }

        if (arcSpec == null || (lastModified != null && currentModDate != null && DateUtils.before(lastModified, currentModDate))) {
            lastModified = currentModDate;
            log.info("Initializing ArcSpec...");
            arcSpec = GetFreshInstance();

            try {
                if (arcSpec != null) {
                    final String cachePath = arcSpec.getGlobalCachePath();
                    if (StringUtils.isNotBlank(cachePath)) {
                        final File arcSpecFile       = new File(cachePath, "archive_specification.xml");
                        final File arcSpecFileFolder = arcSpecFile.getParentFile();
                        if (!arcSpecFileFolder.exists() && !arcSpecFileFolder.mkdirs()) {
                            throw new RuntimeException("Failed to create working file " + arcSpecFile.getAbsolutePath() + ", please check permissions and file system.");
                        }
                        log.debug("Initializing arcspec to cache file {}", arcSpecFile.getAbsolutePath());
                        try (FileWriter writer = new FileWriter(arcSpecFile)) {
                            arcSpec.toXML(writer, true);
                        }
                    }
                }
            } catch (IllegalArgumentException | IOException | SAXException e) {
                log.error("", e);
            }

            log.debug("Done writing out arc spec.");
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

    public synchronized static void Reset() {
        arcSpec = null;
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
}
