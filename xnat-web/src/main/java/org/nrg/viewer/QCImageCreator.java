/*
 * org.nrg.viewer.QCImageCreator
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */

package org.nrg.viewer;

import java.io.File;

import org.apache.log4j.Logger;
import org.nrg.pipeline.XnatPipelineLauncher;
import org.nrg.plexiViewer.lite.xml.PlexiViewerSpecForSession;
import org.nrg.plexiViewer.manager.PlexiSpecDocReader;
import org.nrg.xdat.om.XnatMrsessiondata;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

public class QCImageCreator {

    XnatMrsessiondata mrSession;
    UserI user;
    static Logger logger = Logger.getLogger(QCImageCreator.class);
    
    public QCImageCreator(XnatMrsessiondata mrSession, UserI user) {
        this.mrSession = mrSession;
        this.user = user;
    }
        
    
    public boolean createQCImagesForScans() throws Exception {

        XnatPipelineLauncher xnatPipelineLauncher = new XnatPipelineLauncher(user);
        xnatPipelineLauncher.setAdmin_email(AdminUtils.getAdminEmailId());
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
	    xnatPipelineLauncher.setParameter("mailhost", AdminUtils.getMailServer());
	    xnatPipelineLauncher.setParameter("useremail", user.getEmail());
	    xnatPipelineLauncher.setParameter("adminemail", AdminUtils.getAdminEmailId());


        return xnatPipelineLauncher.launch(null);
    }
    
    public static String GetPathToQCThumbnailFile(XnatMrsessiondata mrSession, String mrScanId) {
        PlexiViewerSpecForSession viewerSpec = PlexiSpecDocReader.GetInstance().getSpecDoc(mrSession.getSessionType());
        return viewerSpec.getThumbnailArchiveLocation() + File.separator + mrSession.getId() +"_" + mrScanId + "_qc_t.gif";
    }

    public static String GetPathToQCFile(XnatMrsessiondata mrSession, String mrScanId) {
        PlexiViewerSpecForSession viewerSpec = PlexiSpecDocReader.GetInstance().getSpecDoc(mrSession.getSessionType());
        String rtn =  viewerSpec.getThumbnailArchiveLocation() + File.separator + mrSession.getId() +"_" + mrScanId + "_qc.gif";
        return rtn;
    }
    
    
    public static String GetSnapshotPathForSession(String sessionArchivePath) {
    	return sessionArchivePath + "SNAPSHOTS";
    }
    
    
    public static String getQCThumbnailPathForSession(String project) {
    	String path = null;
    	if (project!=null){
            path= ArcSpecManager.GetInstance().getCachePathForProject(project);
         }else{
            path= ArcSpecManager.GetInstance().getGlobalCachePath();
         }
    	 String thumb_path = path +"Thumbnail/";
    	return thumb_path;
    }
    
    public static String getQCCachePathForSession(String project) {
    	String path = null;
    	if (project!=null){
            path= ArcSpecManager.GetInstance().getCachePathForProject(project);
         }else{
            path= ArcSpecManager.GetInstance().getGlobalCachePath();
         }
         return path;
    }
}
