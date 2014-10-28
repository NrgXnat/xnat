/*
 * org.nrg.xdat.turbine.modules.screens.XDATScreen_report_xdat_user
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 12/18/13 8:21 AM
 */


package org.nrg.xdat.turbine.modules.screens;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.mail.services.EmailRequestLogService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.UserAuthI;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.security.UserI;

/**
 * @author Tim
 *
 */
public class XDATScreen_report_xdat_user extends AdminReport {
	static Logger logger = Logger.getLogger(DefaultReport.class);
    public void finalProcessing(RunData data,Context context)
    {
        try {            
        	
            UserI tempUser = Users.getUser(item.getStringProperty("login"));
			context.put("userObject",tempUser);
			context.put("userObjectHelper",UserHelper.getUserHelperService(tempUser));
            context.put("allGroups",Groups.getAllGroups());
            
            // Does the user hanve any failed login attempts?
            boolean hasFailedLoginAttempts = false;
            List<XdatUserAuth> auths = XDAT.getXdatUserAuthService().getUsersByName(tempUser.getUsername());
            for (UserAuthI auth : auths) {
                if (auth.getFailedLoginAttempts() > 0) {
                    hasFailedLoginAttempts = true;
                }
            }
            context.put("hasFailedLoginAttempts", hasFailedLoginAttempts);
            
            // Has the user been blocked from requesting emails? (Resend Email Verification / Reset password)
            final EmailRequestLogService requests = XDAT.getContextService().getBean(EmailRequestLogService.class);
            context.put("emailRequestsBlocked", requests.isEmailBlocked(tempUser.getEmail()));

            context.put("allRoles",Roles.getRoles());

        } catch (Exception e) {
            logger.error("",e);
        }
    }
}

