/*
 * web: org.nrg.viewer.QCImageCreator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.viewer;

import org.nrg.pipeline.XnatPipelineLauncher;
import org.nrg.xnat.plexiviewer.lite.xml.PlexiViewerSpecForSession;
import org.nrg.xnat.plexiviewer.manager.PlexiSpecDocReader;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatMrsessiondata;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import java.io.File;

public class QCImageCreator {

    XnatMrsessiondata mrSession;
    UserI user;

    public QCImageCreator(XnatMrsessiondata mrSession, UserI user) {
        this.mrSession = mrSession;
        this.user = user;
    }
        
    
    public boolean createQCImagesForScans() throws Exception {

        XnatPipelineLauncher xnatPipelineLauncher = new XnatPipelineLauncher(user);
        xnatPipelineLauncher.setAdmin_email(XDAT.getSiteConfigPreferences().getAdminEmail());
        xnatPipelineLauncher.setAlwaysEmailAdmin(ArcSpecManager.GetInstance().getEmailspecifications_pipeline());
        xnatPipelineLauncher.setWaitFor(true);
        String pipelineName = "images/WebBasedQCImageCreator.xml";
        xnatPipelineLauncher.setPipelineName(pipelineName);
        xnatPipelineLauncher.setId(mrSession.getId());
        xnatPipelineLauncher.setDataType(mrSession.getXSIType());
        xnatPipelineLauncher.setExternalId(mrSession.getProject());
        xnatPipelineLauncher.setLabel(mrSession.getLabel());
        
        xnatPipelineLauncher.setParameter("sessionLabel", mrSession.getLabel());
        xnatPipelineLauncher.setParameter("xnat_project", mrSession.getProject());
        xnatPipelineLauncher.setParameter("session", mrSession.getId() );
        xnatPipelineLauncher.setParameter("notify", "0" );
	    xnatPipelineLauncher.setParameter("xnatserver", TurbineUtils.GetSystemName());
	    xnatPipelineLauncher.setParameter("mailhost", XDAT.getNotificationsPreferences().getSmtpServer().getHostname());
	    xnatPipelineLauncher.setParameter("useremail", user.getEmail());
	    xnatPipelineLauncher.setParameter("adminemail", XDAT.getSiteConfigPreferences().getAdminEmail());


        return xnatPipelineLauncher.launch(null);
    }
    
    public static String GetPathToQCThumbnailFile(XnatMrsessiondata mrSession, String mrScanId) {
        PlexiViewerSpecForSession viewerSpec = PlexiSpecDocReader.GetInstance().getSpecDoc(mrSession.getSessionType());
        return viewerSpec.getThumbnailArchiveLocation() + File.separator + mrSession.getId() +"_" + mrScanId + "_qc_t.gif";
    }

    public static String GetPathToQCFile(XnatMrsessiondata mrSession, String mrScanId) {
        PlexiViewerSpecForSession viewerSpec = PlexiSpecDocReader.GetInstance().getSpecDoc(mrSession.getSessionType());
        return viewerSpec.getThumbnailArchiveLocation() + File.separator + mrSession.getId() + "_" + mrScanId + "_qc.gif";
    }
    
    
    public static String GetSnapshotPathForSession(String sessionArchivePath) {
    	return sessionArchivePath + "SNAPSHOTS";
    }
    
    
    public static String getQCThumbnailPathForSession(String project) {
    	String path;
    	if (project!=null){
            path= ArcSpecManager.GetInstance().getCachePathForProject(project);
         }else{
            path= ArcSpecManager.GetInstance().getGlobalCachePath();
         }
        return path + "Thumbnail/";
    }
    
    public static String getQCCachePathForSession(String project) {
    	String path;
    	if (project!=null){
            path= ArcSpecManager.GetInstance().getCachePathForProject(project);
         }else{
            path= ArcSpecManager.GetInstance().getGlobalCachePath();
         }
         return path;
    }
}
