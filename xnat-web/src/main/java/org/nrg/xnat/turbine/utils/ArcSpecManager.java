/*
 * org.nrg.xnat.turbine.utils.ArcSpecManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.utils;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.ArcArchivespecification;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.helpers.prearchive.PrearcConfig;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

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
                if (arcSpec!=null){
                    String cachePath = arcSpec.getGlobalCachePath();
                    if (cachePath!=null){
                        File f = new File(cachePath,"archive_specification.xml");
                        f.getParentFile().mkdirs();
                        FileWriter fw = new FileWriter(f);

                        arcSpec.toXML(fw, true);
                        fw.flush();
                        fw.close();
                    }
                }
            } catch (IllegalArgumentException | IOException | SAXException e) {
                logger.error("",e);
            }
            logger.debug("Done writing out arc spec.");
   
            if(dbInit){
                PrearcConfig prearcConfig = XDAT.getContextService().getBean(PrearcConfig.class);
	            try {
	    			PrearcDatabase.initDatabase(prearcConfig.isReloadPrearcDatabaseOnApplicationStartup());
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
        final SiteConfigPreferences preferences = XDAT.getSiteConfigPreferences();
        if (StringUtils.isNotBlank(preferences.getAdminEmail())) {
            if (logger.isInfoEnabled()) {
                logger.info("Setting site admin email to: {}", preferences.getAdminEmail());
            }
            arcSpec.setSiteAdminEmail(preferences.getAdminEmail());
        }

        if (StringUtils.isNotBlank(preferences.getSiteId())) {
            if (logger.isInfoEnabled()) {
                logger.info("Setting site ID to: {}", preferences.getSiteId());
            }
            arcSpec.setSiteId(preferences.getSiteId());
        }

        if (StringUtils.isNotBlank(preferences.getSiteUrl())) {
            if (logger.isInfoEnabled()) {
                logger.info("Setting site URL to: {}", preferences.getSiteUrl());
            }
            arcSpec.setSiteUrl(preferences.getSiteUrl());
        }

        final Map<String, String> smtpServer = preferences.getSmtpServer();
        if (smtpServer != null && smtpServer.containsKey("host")) {
            if (logger.isInfoEnabled()) {
                logger.info("Setting SMTP host to: {}", smtpServer.get("host"));
            }
            arcSpec.setSmtpHost(smtpServer.get("host"));
        }

        if (logger.isInfoEnabled()) {
            logger.info("Setting enable new registrations to: {}", preferences.getUserRegistration());
        }
        arcSpec.setEnableNewRegistrations(preferences.getUserRegistration());

        if (logger.isInfoEnabled()) {
            logger.info("Setting reguire login to: {}", preferences.getRequireLogin());
        }
        arcSpec.setRequireLogin(preferences.getRequireLogin());

        if (StringUtils.isNotBlank(preferences.getPipelinePath())) {
            if (logger.isInfoEnabled()) {
                logger.info("Setting pipeline path to: {}", preferences.getPipelinePath());
            }
            arcSpec.setProperty("globalPaths/pipelinePath", preferences.getPipelinePath());
        }

        if (StringUtils.isNotBlank(preferences.getArchivePath())) {
            if (logger.isInfoEnabled()) {
                logger.info("Setting archive path to: {}", preferences.getArchivePath());
            }
            arcSpec.setProperty("globalPaths/archivePath", preferences.getArchivePath());
        }

        if (StringUtils.isNotBlank(preferences.getPrearchivePath())) {
            if (logger.isInfoEnabled()) {
                logger.info("Setting prearchive path to: {}", preferences.getPrearchivePath());
            }
            arcSpec.setProperty("globalPaths/prearchivePath", preferences.getPrearchivePath());
        }

        if (StringUtils.isNotBlank(preferences.getCachePath())) {
            if (logger.isInfoEnabled()) {
                logger.info("Setting cache path to: {}", preferences.getCachePath());
            }
            arcSpec.setProperty("globalPaths/cachePath", preferences.getCachePath());
        }

        if (StringUtils.isNotBlank(preferences.getFtpPath())) {
            if (logger.isInfoEnabled()) {
                logger.info("Setting FTP path to: {}", preferences.getFtpPath());
            }
            arcSpec.setProperty("globalPaths/ftpPath", preferences.getFtpPath());
        }

        if (StringUtils.isNotBlank(preferences.getBuildPath())) {
            if (logger.isInfoEnabled()) {
                logger.info("Setting build path to: {}", preferences.getBuildPath());
            }
            arcSpec.setProperty("globalPaths/buildPath", preferences.getBuildPath());
        }

        if (logger.isInfoEnabled()) {
            logger.info("Setting enable CSRF token to: {}", preferences.getEnableCsrfToken());
        }
        arcSpec.setEnableCsrfToken(preferences.getEnableCsrfToken());

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
