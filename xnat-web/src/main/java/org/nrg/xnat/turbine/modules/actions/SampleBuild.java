/*
 * org.nrg.xnat.turbine.modules.actions.SampleBuild
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.pipeline.XnatPipelineLauncher;
import org.nrg.xdat.om.XnatMrsessiondata;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

public class SampleBuild extends SecureAction
{
    static org.apache.log4j.Logger logger = Logger.getLogger(SampleBuild.class);
    
    public void doPerform(RunData data, Context context){
        try {
            ItemI data_item = TurbineUtils.GetItemBySearch(data);
            XnatMrsessiondata mr = new XnatMrsessiondata(data_item);
            String pipelineName = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("pipeline",data));
            if (pipelineName != null) {
                if (!pipelineName.endsWith(".xml")) {
                    pipelineName += ".xml";
                }
                String xnat = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("xnat",data));
                XnatPipelineLauncher pipelineLauncher = new XnatPipelineLauncher(data,context);
                pipelineLauncher.setAdmin_email(AdminUtils.getAdminEmailId());
                pipelineLauncher.setAlwaysEmailAdmin(ArcSpecManager.GetInstance().getEmailspecifications_pipeline());
                pipelineLauncher.setId(mr.getId());
                pipelineLauncher.setDataType("xnat:mrSessionData");
                pipelineLauncher.setPipelineName(pipelineName);
                pipelineLauncher.setParameter("sessionId",mr.getId());
                pipelineLauncher.setParameter("xnat",xnat);
                String emailsStr = TurbineUtils.getUser(data).getEmail() + "," + ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("emailField",data));
                String[] emails = emailsStr.trim().split(",");
                for (int i = 0 ; i < emails.length; i++)
                     pipelineLauncher.notify(emails[i]);
                boolean success = pipelineLauncher.launch();
                if (success) {
                    data.setMessage("Build was launched successfully");
                    data.setScreenTemplate("ClosePage.vm");
                }else {
                    data.setMessage("Build process failed");
                    data.setScreenTemplate("Error.vm");
                }
            }
        }catch(Exception e){
            logger.info(e.getMessage(),e);
        }
    }
   

}
