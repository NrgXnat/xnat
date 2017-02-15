/*
 * web: org.nrg.xnat.turbine.modules.actions.RequestAccess
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.turbine.utils.ProjectAccessRequest;

import java.io.StringWriter;
import java.util.ArrayList;

public class RequestAccess extends SecureAction {
    static Logger logger = Logger.getLogger(RequestAccess.class);

    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        String p = ((String) TurbineUtils.GetPassedParameter("project",data));
        XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(p, null, false);

        String access_level = ((String) TurbineUtils.GetPassedParameter("access_level",data));
        String comments = ((String) TurbineUtils.GetPassedParameter("comments",data));

        UserI user = TurbineUtils.getUser(data);

        ProjectAccessRequest.CreatePAR(project.getId(), access_level, user);
                
        context.put("user",user);
        context.put("server",TurbineUtils.GetFullServerPath());
        context.put("siteLogoPath", XDAT.getSiteLogoPath());
        context.put("process","Transfer to the archive.");
        context.put("system",TurbineUtils.GetSystemName());
        context.put("admin_email",XDAT.getSiteConfigPreferences().getAdminEmail());
        context.put("projectOM",project);
        context.put("access_level",access_level);
        context.put("comments",comments);
        context.put("displayManager", DisplayManager.GetInstance());
        StringWriter sw = new StringWriter();
        Template template;
		try {
			template = Velocity.getTemplate("/screens/RequestProjectAccessEmail.vm");
        template.merge(context,sw);
		} catch (Exception exception) {
            logger.error("Unable to send mail", exception);
            throw exception;
		}

        String message= sw.toString();

        ArrayList<String> ownerEmails;
		try {
			ownerEmails = project.getOwnerEmails();
		} catch (Exception exception) {
            logger.error("Unable to send mail", exception);
            throw exception;
		}

		String[] to = {};
        if (ownerEmails != null && ownerEmails.size() > 0) {
        	to = ownerEmails.toArray(new String[ownerEmails.size()]);
        }

        String[] bcc = null;
        if(ArcSpecManager.GetInstance().getEmailspecifications_projectAccess()){
        	bcc = new String[] { XDAT.getSiteConfigPreferences().getAdminEmail() };
        }
        
        String from = XDAT.getSiteConfigPreferences().getAdminEmail();
        String subject = TurbineUtils.GetSystemName() + " Access Request for " + project.getName();

        data.setMessage("Access request sent.");

        try {
			XDAT.getMailService().sendHtmlMessage(from, to, null, bcc, subject, message);
        } catch (Exception exception) {
            logger.error("Send failed. Retrying by sending each email individually.", exception);
            int successfulSends = 0;
            for (String recipient : to) {
                try {
                    XDAT.getMailService().sendHtmlMessage(from, new String[]{recipient}, null, bcc, subject, message);
                    successfulSends++;
                } catch (Exception e) {
                    logger.error("Unable to send mail to " + recipient + ".", e);
                }
            }
            if (successfulSends == 0) {
                logger.error("Unable to send mail", exception);
                data.setMessage("No project owners have emails which could receive the access request. Please contact the system administrator for additional assistance.");
            }
        }

        data.setScreenTemplate("Index.vm");
    }
}
