/*
 * web: org.nrg.xnat.turbine.utils.ArcSpecManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.utils;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.ArcProjectI;
import org.nrg.xdat.om.ArcArchivespecification;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.preferences.SmtpServer;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author timo
 *
 */
public class ArcSpecManager {
    private static final Logger logger = LoggerFactory.getLogger(ArcSpecManager.class);
    private static ArcArchivespecification arcSpec = null;

    public synchronized static ArcArchivespecification GetFreshInstance() {
		ArcArchivespecification arcSpec = null;
        logger.warn("Getting Fresh ArcSpec...");
		ArrayList<ArcArchivespecification> allSpecs = ArcArchivespecification.getAllArcArchivespecifications(null,false);
	    if (allSpecs.size()>0) {
	        arcSpec = allSpecs.get(0);
        }
	    return arcSpec;
	}
    
    public synchronized static  ArcArchivespecification GetInstance(){
    	return GetInstance(true);
    }
    
    public synchronized static  ArcArchivespecification GetInstance(boolean dbInit){
        if (arcSpec==null){
            logger.info("Initializing ArcSpec...");
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
                        logger.debug("Initializing arcspec to cache file {}", arcSpecFile.getAbsolutePath());
                        try (FileWriter writer = new FileWriter(arcSpecFile)) {
                            arcSpec.toXML(writer, true);
                        }
                    }
                }
            } catch (IllegalArgumentException | IOException | SAXException e) {
                logger.error("", e);
            }
            logger.debug("Done writing out arc spec.");
   
            if(dbInit){
	            try {
                    PrearcDatabase.initDatabase(XDAT.getBoolSiteConfigurationProperty("reloadPrearcDatabaseOnStartup", false));
	    		} catch (Exception e) {
	    			logger.error("",e);
	    		}
            }
        }
        
        return arcSpec;
    }

    public synchronized static  void Reset(){
        arcSpec=null;
    }

    public synchronized static ArcArchivespecification initialize(final UserI user) throws Exception {
        arcSpec = new ArcArchivespecification(user);
        final SiteConfigPreferences siteConfigPreferences = XDAT.getSiteConfigPreferences();
        final NotificationsPreferences notificationsPreferences = XDAT.getNotificationsPreferences();
        if (StringUtils.isNotBlank(siteConfigPreferences.getAdminEmail())) {
            if (logger.isInfoEnabled()) {
                logger.info("Setting site admin email to: {}", siteConfigPreferences.getAdminEmail());
            }
            arcSpec.setSiteAdminEmail(siteConfigPreferences.getAdminEmail());
        }

        if (StringUtils.isNotBlank(siteConfigPreferences.getSiteId())) {
            if (logger.isInfoEnabled()) {
                logger.info("Setting site ID to: {}", siteConfigPreferences.getSiteId());
            }
            arcSpec.setSiteId(siteConfigPreferences.getSiteId());
        }

        if (StringUtils.isNotBlank(siteConfigPreferences.getSiteUrl())) {
            if (logger.isInfoEnabled()) {
                logger.info("Setting site URL to: {}", siteConfigPreferences.getSiteUrl());
            }
            arcSpec.setSiteUrl(siteConfigPreferences.getSiteUrl());
        }

        final SmtpServer smtpServer = notificationsPreferences.getSmtpServer();
        if (smtpServer != null) {
            final String hostname = smtpServer.getHostname();
            logger.info("Setting SMTP host to: {}", hostname);
            arcSpec.setSmtpHost(hostname);
        }

        if (logger.isInfoEnabled()) {
            logger.info("Setting enable new registrations to: {}", siteConfigPreferences.getUserRegistration());
        }
        arcSpec.setEnableNewRegistrations(siteConfigPreferences.getUserRegistration());

        if (logger.isInfoEnabled()) {
            logger.info("Setting reguire login to: {}", siteConfigPreferences.getRequireLogin());
        }
        arcSpec.setRequireLogin(siteConfigPreferences.getRequireLogin());

        if (StringUtils.isNotBlank(siteConfigPreferences.getPipelinePath())) {
            if (logger.isInfoEnabled()) {
                logger.info("Setting pipeline path to: {}", siteConfigPreferences.getPipelinePath());
            }
            arcSpec.setProperty("globalPaths/pipelinePath", siteConfigPreferences.getPipelinePath());
        }

        if (StringUtils.isNotBlank(siteConfigPreferences.getArchivePath())) {
            if (logger.isInfoEnabled()) {
                logger.info("Setting archive path to: {}", siteConfigPreferences.getArchivePath());
            }
            arcSpec.setProperty("globalPaths/archivePath", siteConfigPreferences.getArchivePath());
        }

        if (StringUtils.isNotBlank(siteConfigPreferences.getPrearchivePath())) {
            if (logger.isInfoEnabled()) {
                logger.info("Setting prearchive path to: {}", siteConfigPreferences.getPrearchivePath());
            }
            arcSpec.setProperty("globalPaths/prearchivePath", siteConfigPreferences.getPrearchivePath());
        }

        if (StringUtils.isNotBlank(siteConfigPreferences.getCachePath())) {
            if (logger.isInfoEnabled()) {
                logger.info("Setting cache path to: {}", siteConfigPreferences.getCachePath());
            }
            arcSpec.setProperty("globalPaths/cachePath", siteConfigPreferences.getCachePath());
        }

        if (StringUtils.isNotBlank(siteConfigPreferences.getFtpPath())) {
            if (logger.isInfoEnabled()) {
                logger.info("Setting FTP path to: {}", siteConfigPreferences.getFtpPath());
            }
            arcSpec.setProperty("globalPaths/ftpPath", siteConfigPreferences.getFtpPath());
        }

        if (StringUtils.isNotBlank(siteConfigPreferences.getBuildPath())) {
            if (logger.isInfoEnabled()) {
                logger.info("Setting build path to: {}", siteConfigPreferences.getBuildPath());
            }
            arcSpec.setProperty("globalPaths/buildPath", siteConfigPreferences.getBuildPath());
        }

        List<ArcProjectI> projList =arcSpec.getProjects_project();


        for(ArcProjectI proj : projList){
            org.nrg.xdat.model.ArcPathinfoI paths = proj.getPaths();
            paths.setPipelinepath(Paths.get(siteConfigPreferences.getPipelinePath(),proj.getId()).toString());
            paths.setArchivepath(Paths.get(siteConfigPreferences.getArchivePath(),proj.getId()).toString());
            paths.setPrearchivepath(Paths.get(siteConfigPreferences.getPrearchivePath(),proj.getId()).toString());
            paths.setCachepath(Paths.get(siteConfigPreferences.getCachePath(),proj.getId()).toString());
            paths.setFtppath(Paths.get(siteConfigPreferences.getFtpPath(),proj.getId()).toString());
            paths.setBuildpath(Paths.get(siteConfigPreferences.getBuildPath(),proj.getId()).toString());
            proj.setPaths(paths);
        }

        if (logger.isInfoEnabled()) {
            logger.info("Setting enable CSRF token to: {}", siteConfigPreferences.getEnableCsrfToken());
        }
        arcSpec.setEnableCsrfToken(siteConfigPreferences.getEnableCsrfToken());

        if (logger.isInfoEnabled()) {
            // logger.info("Saving arcspec: {}", displayArcSpec(arcSpec));
            logger.info("Saving arcspec");
        }
        save(arcSpec, user, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.PROCESS, "Initialized archive specifications."));
        return arcSpec;
    }

//    private static String displayArcSpec(final ArcArchivespecification arcSpec) {
//        return "TBD.";
//    }

    public static boolean allowTransferEmail(){
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
