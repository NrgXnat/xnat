/*
 * org.nrg.xnat.turbine.modules.screens.QualityControl
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.viewer.QCImageCreator;
import org.nrg.xdat.turbine.modules.screens.FileScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import java.io.File;

public class QualityControl extends FileScreen {

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.FileScreen#getDownloadFile(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public File getDownloadFile(RunData data, Context context) {
    	String extension=(String)TurbineUtils.GetPassedParameter("extension",data);
    	
    	if(extension==null || extension.equals("") || !extension.toLowerCase().endsWith(".gif")){
    		logger.error("Invalid file access prevented. Extension '" + extension + "' not allowed.");
    		return null;
    	}
    	
    	String session_label=(String)TurbineUtils.GetPassedParameter("session_label",data);
    	String session_id=(String)TurbineUtils.GetPassedParameter("session_id",data);
    	String scan=(String)TurbineUtils.GetPassedParameter("scan",data);
    	String idImg=session_id+"_" + scan + "_qc" + extension;
    	String labelImg=session_label + "_"+scan + "_qc" + extension;
    	
        String project= (String)TurbineUtils.GetPassedParameter("project",data);
        String path = QCImageCreator.getQCThumbnailPathForSession(project);

        File img= new File(path + idImg);
        if (!img.exists()) {
        	String prearcP=ArcSpecManager.GetInstance().getPrearchivePathForProject(project);
        	
        	File prearc=new File(prearcP);
        	if(prearc.exists()){
        		for(File f: prearc.listFiles()){
        			if(f.isDirectory()){
        				img=new File(f,session_label + "/RAW/"+ scan + "/" + labelImg);
        				if(img.exists())
        				{
        					break;
        				}
        				img=new File(f,session_label + "/RAW/"+ scan + "/SNAPSHOTS/" + labelImg);
        				if(img.exists())
        				{
        					break;
        				}
        				img=new File(f,session_label + "/RAW/"+ scan + "/" + idImg);
        				if(img.exists())
        				{
        					break;
        				}
        				img=new File(f,session_label + "/RAW/"+ scan + "/SNAPSHOTS/" + idImg);
        				if(img.exists())
        				{
        					break;
        				}
        				img=new File(f,session_label + "/SCANS/"+ scan + "/" + labelImg);
        				if(img.exists())
        				{
        					break;
        				}
        				img=new File(f,session_label + "/SCANS/"+ scan + "/SNAPSHOTS/" + labelImg);
        				if(img.exists())
        				{
        					break;
        				}
        				img=new File(f,session_label + "/SCANS/"+ scan + "/" + idImg);
        				if(img.exists())
        				{
        					break;
        				}
        				img=new File(f,session_label + "/SCANS/"+ scan + "/SNAPSHOTS/" + idImg);
        				if(img.exists())
        				{
        					break;
        				}
        			}
        		}
        	}
        }
        return img;
    }
    
    public void logAccess(RunData data)
    {
    }

}