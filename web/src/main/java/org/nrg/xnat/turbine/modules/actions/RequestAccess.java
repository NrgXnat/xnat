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
import org.nrg.action.ActionException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.turbine.utils.ProjectAccessRequest;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Locale;

public class RequestAccess extends SecureAction {
    static Logger logger = Logger.getLogger(RequestAccess.class);

    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        String p = ((String) TurbineUtils.GetPassedParameter("project",data));
        XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(p, null, false);

        String access_level = ((String) TurbineUtils.GetPassedParameter("access_level",data));
        String comments = ((String) TurbineUtils.GetPassedParameter("comments",data));

        UserI user = TurbineUtils.getUser(data);

        if(user.isGuest()) {
            data.setMessage("You must log in before requesting access to a project.");
        }
        else{
            ProjectAccessRequest.CreatePAR(project.getId(), access_level, user);

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

            if (XDAT.getNotificationsPreferences().getSmtpEnabled()) {
                String body = XDAT.getNotificationsPreferences().getEmailMessageProjectRequestAccess();
                body = XDAT.getNotificationsPreferences().replaceCommonAnchorTags(body, user);
                body = body.replaceAll("USER_EMAIL", user.getEmail());
                if (comments == null) {
                    body = body.replaceAll("RQA_COMMENTS", "");
                } else {
                    body = body.replaceAll("RQA_COMMENTS", comments);
                }
                body = body.replaceAll("PROJECT_NAME", project.getName());
                body = body.replaceAll("RQ_ACCESS_LEVEL", access_level);

                if (access_level.toLowerCase().equals("owner")) {
                    body = body.replaceAll("LIST_PERMISSIONS", "read, edit and manage anything affiliated with this project.");
                } else if(access_level.toLowerCase().equals("member")) {
                    body = body.replaceAll("LIST_PERMISSIONS", "read and edit the project.");
                } else if (access_level.toLowerCase().equals("collaborator")) {
                    body = body.replaceAll("LIST_PERMISSIONS", "read the project and experiment data. The user will NOT be able to edit the data.");
                } else {
                    String permission = "perform the actions as defined by the " + access_level + " custom group.";
                    body = body.replaceAll("LIST_PERMISSIONS", permission);
                }

                body = body.replaceAll("LIST_PERMISSIONS", "permissionLevel");

                final String respondAccessUrl = TurbineUtils.GetFullServerPath() +"/app/template/RequestProjectAccessForm.vm/project/" + project.getId() + "/id/" + user.getID().toString() + "/access_level/" + access_level;

                String accessUrl = "<a href=\"" + respondAccessUrl + "\">" + respondAccessUrl + "</a>";

                body = body.replaceAll("ACCESS_URL", accessUrl);


                String subject = TurbineUtils.GetSystemName() + " Access Request for " + project.getName();

                String[] bcc = null;
                if (ArcSpecManager.GetInstance().getEmailspecifications_projectAccess()) {
                    bcc = new String[]{XDAT.getSiteConfigPreferences().getAdminEmail()};
                }

                String from = XDAT.getSiteConfigPreferences().getAdminEmail();

                data.setMessage("Access request sent.");

                try {
                    XDAT.getMailService().sendHtmlMessage(from, to, null, bcc, subject, body);
                } catch (Exception exception) {
                    logger.error("Send failed. Retrying by sending each email individually.", exception);
                    int successfulSends = 0;
                    for (String recipient : to) {
                        try {
                            XDAT.getMailService().sendHtmlMessage(from, new String[]{recipient}, null, bcc, subject, body);
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
            }
        }

        data.setScreenTemplate("Index.vm");
    }
}
