/*
 * org.nrg.xnat.turbine.modules.screens.UploadImages
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.screens;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

public class UploadImages extends SecureScreen {
    static org.apache.log4j.Logger logger = Logger.getLogger(UploadImages.class);

    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        Date d = Calendar.getInstance().getTime();
        
        StringBuffer sb = new StringBuffer();
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("yyyyMMdd_HHmmss");
        sb.append(formatter.format(d));
        context.put("uploadID", sb.toString());
        
        UserI user = TurbineUtils.getUser(data);
        String cachepath= ArcSpecManager.GetInstance().getGlobalCachePath();
        
        cachepath += "uploads" + File.separator + user.getLogin() + File.separator;

        java.text.NumberFormat nf = NumberFormat.getInstance();
        ArrayList uploadedFiles = new ArrayList();
        formatter = new java.text.SimpleDateFormat ("MM/dd/yyyy HH:mm:ss");
        File dir = new File(cachepath);
        File[] listFiles = dir.listFiles();
        if (listFiles !=null)
        {
            for(int i=0;i<listFiles.length;i++){
                if (listFiles[i].isDirectory())
                {
                    //root
                    File[] children = listFiles[i].listFiles();

                    for(int j=0;j<children.length;j++){
                        ArrayList file = new ArrayList();
                        file.add(children[j].getName());
                        file.add(formatter.format(children[j].lastModified()));
                        file.add((children[j].length()/1024) +" KB");
                        uploadedFiles.add(file);
                    }
                }
            }
        }
        
        context.put("image_type", TurbineUtils.GetPassedParameter("image_type", data, "ECAT"));
        context.put("uploadedFiles", uploadedFiles);
    }

}
