/*
 * web: org.nrg.xnat.turbine.modules.actions.CompleteUpload
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.parser.ParameterParser;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.UploadManager;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

public class CompleteUpload extends SecureAction {
    static Logger logger = Logger.getLogger(CompleteUpload.class);
    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        ParameterParser params = data.getParameters();
        HttpSession session = data.getSession();
        UserI user = TurbineUtils.getUser(data);
        String cachepath= ArcSpecManager.GetInstance().getGlobalCachePath();;
        Date d = Calendar.getInstance().getTime();
        StringBuffer sb = new StringBuffer();
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("yyyyMMdd_HHmmss");
        sb.append(formatter.format(d));
        
        String rootPath =null;
        
        if (params.containsKey("project"))
        {
            String p= params.get("project");
            try {
                rootPath = ArcSpecManager.GetInstance().getPrearchivePathForProject(p);
            } catch (Throwable e) {
                logger.error("",e);
            }
        }
        
        String uploadsPath =cachepath + "uploads" + File.separator + user.getLogin() + File.separator ;
        File dir = new File(uploadsPath);
        
        String prearchive_path = rootPath;
        String image_type = "MR";
        if (params.getString("image_type")!=null){
            image_type = params.getString("image_type");
        }
        
        UploadManager um = new UploadManager(user,image_type, cachepath + "batch" + File.separator + user.getLogin() + File.separator+ image_type + File.separator + sb.toString() + File.separator,TurbineUtils.GetFullServerPath());
        try {
            File[] listFiles = dir.listFiles();
            for(int i=0;i<listFiles.length;i++){
                if (listFiles[i].isDirectory())
                {
                    //root
                    File[] children = listFiles[i].listFiles();
                    for(int k=0;k<children.length;k++){
                        um.addFile(children[k]);
                    }
                }
            }
        } catch (Throwable e) {
            logger.error("",e);
        }
        
        if (um.size()>0)
        {
            System.out.println("Starting Upload Manager. ");
            Thread thread = new Thread(um);
            thread.start();
            this.redirectToScreen("UploadProcessing.vm", data);
        }else{
            this.redirectToScreen("UploadImages.vm", data);
        }
        
    }

}
