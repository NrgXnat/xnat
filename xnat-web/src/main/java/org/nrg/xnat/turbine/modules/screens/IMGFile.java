/*
 * org.nrg.xnat.turbine.modules.screens.IMGFile
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.screens;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.screens.FileScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import java.io.File;

public class IMGFile  extends FileScreen {
    static org.apache.log4j.Logger logger = Logger.getLogger(IMGFile.class);

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.FileScreen#getDownloadFile(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public File getDownloadFile(RunData data, Context context) {
        String fileName= (String)TurbineUtils.GetPassedParameter("file_name",data);
        String project= (String)TurbineUtils.GetPassedParameter("project",data);
        System.out.println("FILE: " + fileName);
        if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg") || fileName.toLowerCase().endsWith(".gif") || fileName.toLowerCase().endsWith(".png") )
        {
            String path =null;
            if (project!=null){
               path= ArcSpecManager.GetInstance().getArchivePathForProject(project);
            }else{
               path= ArcSpecManager.GetInstance().getGlobalArchivePath();
            }
            
            
            File f = new File(fileName);
            if (f.exists())
                return f;
            else {
                f = new File(path + fileName);
                return f;
            }
        }else{
            logger.error("WARNING: Non image file requested. Ignoring request.\n" + fileName);
            return null;
        }
    }
    
    public void logAccess(RunData data)
    {
    }

}