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

//            if (arcSpec!=null){
//
//                if (arcSpec.getSiteAdminEmail()!=null && !arcSpec.getSiteAdminEmail().equals("")){
//                    XFT.SetAdminEmail(arcSpec.getSiteAdminEmail());
//                }else{
//                    arcSpec.setSiteAdminEmail(XDAT.getSiteConfigPreferences().getAdminEmail());
//                }
//
//                if (arcSpec.getSiteUrl()!=null && !arcSpec.getSiteUrl().equals("")){
//                    XFT.SetSiteURL(arcSpec.getSiteUrl());
//                }else{
//                    arcSpec.setSiteUrl(XDAT.getSiteConfigPreferences().getSiteUrl());
//                }
//
//                if (arcSpec.getSiteId()!=null && !arcSpec.getSiteId().equals("")){
//                    XFT.SetSiteID(arcSpec.getSiteId());
//                }else{
//                    arcSpec.setSiteId("");
//                }
//
//                if (arcSpec.getSmtpHost()!=null && !arcSpec.getSmtpHost().equals("")){
//                    XFT.SetAdminEmailHost(arcSpec.getSmtpHost());
//                }else{
//                    arcSpec.setSmtpHost(XFT.GetAdminEmailHost());
//                }
//
//                if (arcSpec.getEnableNewRegistrations()!=null){
//                    XFT.SetUserRegistration(arcSpec.getEnableNewRegistrations().toString());
//                }else{
//                    arcSpec.setEnableNewRegistrations(XDAT.getSiteConfigPreferences().getUserRegistration());
//                }
//
//                if (arcSpec.getRequireLogin()!=null){
//                    XFT.SetRequireLogin(arcSpec.getRequireLogin().toString());
//                }else{
//                    arcSpec.setRequireLogin(XDAT.getSiteConfigPreferences().getRequireLogin());
//                }
//
//                if (arcSpec.getGlobalpaths()!=null && arcSpec.getGlobalpaths().getPipelinepath()!=null){
//                    XFT.SetPipelinePath(arcSpec.getGlobalpaths().getPipelinepath());
//                }else{
//                    if (arcSpec.getGlobalpaths()!=null){
//                        arcSpec.getGlobalpaths().setPipelinepath(XDAT.getSiteConfigPreferences().getAdminEmail());
//                    }
//                }
//
//                if (arcSpec.getGlobalpaths()!=null && arcSpec.getGlobalpaths().getArchivepath()!=null){
//                    XFT.SetArchiveRootPath(arcSpec.getGlobalpaths().getArchivepath());
//                }else{
//                    if (arcSpec.getGlobalpaths()!=null && XFT.GetArchiveRootPath()!=null){
//                        arcSpec.getGlobalpaths().setArchivepath(XFT.GetArchiveRootPath());
//                    }
//                }
//
//                if (arcSpec.getGlobalpaths()!=null && arcSpec.getGlobalpaths().getCachepath()!=null){
//                    XFT.SetCachePath(arcSpec.getGlobalpaths().getCachepath());
//                }else{
//                    if (arcSpec.getGlobalpaths()!=null && XDAT.getSiteConfigPreferences().getCachePath()!=null){
//                        arcSpec.getGlobalpaths().setCachepath(XDAT.getSiteConfigPreferences().getCachePath());
//                    }
//                }
//
//                if (arcSpec.getGlobalpaths()!=null && arcSpec.getGlobalpaths().getFtppath()!=null){
//                    XFT.setFtpPath(arcSpec.getGlobalpaths().getFtppath());
//                }else{
//                    if (arcSpec.getGlobalpaths()!=null && XFT.getFtpPath()!=null){
//                        arcSpec.getGlobalpaths().setFtppath(XFT.getFtpPath());
//                    }
//                }
//
//                if (arcSpec.getGlobalpaths()!=null && arcSpec.getGlobalpaths().getBuildpath()!=null){
//                    XFT.setFtpPath(arcSpec.getGlobalpaths().getBuildpath());
//                }else{
//                    if (arcSpec.getGlobalpaths()!=null && XFT.getBuildPath()!=null){
//                        arcSpec.getGlobalpaths().setBuildpath(XFT.getBuildPath());
//                    }
//                }
//
//                if (arcSpec.getGlobalpaths()!=null && arcSpec.getGlobalpaths().getPrearchivepath()!=null){
//                    XFT.SetPrearchivePath(arcSpec.getGlobalpaths().getPrearchivepath());
//                }else{
//                    if (arcSpec.getGlobalpaths()!=null && XFT.GetPrearchivePath()!=null){
//                        arcSpec.getGlobalpaths().setPrearchivepath(XFT.GetPrearchivePath());
//                    }
//                }
//
//
//                //set email defaults
//                if (arcSpec.getEmailspecifications_newUserRegistration()==null){
//                    arcSpec.setEmailspecifications_newUserRegistration(true);
//                }
//                if (arcSpec.getEmailspecifications_pageEmail()==null){
//                    arcSpec.setEmailspecifications_pageEmail(true);
//                }
//                if (arcSpec.getEmailspecifications_pipeline()==null){
//                    arcSpec.setEmailspecifications_pipeline(true);
//                }
//                if (arcSpec.getEmailspecifications_projectAccess()==null){
//                    arcSpec.setEmailspecifications_projectAccess(true);
//                }
//                if (arcSpec.getEmailspecifications_transfer()==null){
//                    arcSpec.setEmailspecifications_transfer(true);
//                }
//                //end email defaults
//
//                if (arcSpec.getEmailspecifications_newUserRegistration()!=null){
//                    AdminUtils.SetNewUserRegistrationsEmail(arcSpec.getEmailspecifications_newUserRegistration());
//                }
//
//                if (arcSpec.getEmailspecifications_pageEmail()!=null){
//                    AdminUtils.SetPageEmail(arcSpec.getEmailspecifications_pageEmail());
//                }
//
//                if (arcSpec.getDcm_appletLink()==null){
//                    arcSpec.setDcm_appletLink(Boolean.TRUE);
//                }
//
//                if (arcSpec.getEnableCsrfToken()!=null){
//                    XFT.SetEnableCsrfToken(arcSpec.getEnableCsrfToken().toString());
//                }else{
//                    arcSpec.setEnableCsrfToken(XDAT.getSiteConfigPreferences().getEnableCsrfToken());
//                }
//            }

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
            arcSpec.setSiteAdminEmail(preferences.getAdminEmail());
        }

        if (StringUtils.isNotBlank(preferences.getSiteId())) {
            arcSpec.setSiteId(preferences.getSiteId());
        }

        if (StringUtils.isNotBlank(preferences.getSiteUrl())) {
            arcSpec.setSiteUrl(preferences.getSiteUrl());
        }

        final Map<String, String> smtpServer = preferences.getSmtpServer();
        if (smtpServer != null && smtpServer.containsKey("host")) {
            arcSpec.setSmtpHost(smtpServer.get("host"));
        }

        arcSpec.setEnableNewRegistrations(preferences.getUserRegistration());

        arcSpec.setRequireLogin(preferences.getRequireLogin());

        if (StringUtils.isNotBlank(preferences.getPipelinePath())) {
            arcSpec.setProperty("globalPaths/pipelinePath", preferences.getPipelinePath());
        }

        if (StringUtils.isNotBlank(preferences.getArchivePath())) {
            arcSpec.setProperty("globalPaths/archivePath", preferences.getArchivePath());
        }

        if (StringUtils.isNotBlank(preferences.getPrearchivePath())) {
            arcSpec.setProperty("globalPaths/prearchivePath", preferences.getPrearchivePath());
        }

        if (StringUtils.isNotBlank(preferences.getCachePath())) {
            arcSpec.setProperty("globalPaths/cachePath", preferences.getCachePath());
        }

        if (StringUtils.isNotBlank(preferences.getFtpPath())) {
            arcSpec.setProperty("globalPaths/ftpPath", preferences.getFtpPath());
        }

        if (StringUtils.isNotBlank(preferences.getBuildPath())) {
            arcSpec.setProperty("globalPaths/buildPath", preferences.getBuildPath());
        }

        arcSpec.setEnableCsrfToken(preferences.getEnableCsrfToken());

        save(arcSpec, user, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.PROCESS, "Initialized archive specifications."));
        return arcSpec;
    }

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
