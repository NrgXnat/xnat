/*
 * org.nrg.xnat.turbine.modules.actions.XDATChangePassword
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/3/14 9:54 AM
 */
package org.nrg.xnat.turbine.modules.actions;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.modules.actions.VelocitySecureAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;

import java.sql.SQLException;

@SuppressWarnings("unused")
public class XDATChangePassword extends VelocitySecureAction {

    /**
     * CGI Parameter for the password
     */
    public static final String CGI_PASSWORD = "xdat:user.primary_password";

    @Override
    public void doPerform(RunData data, Context context) throws Exception {

        final String method = data.getRequest().getMethod();
        if (!StringUtils.equalsIgnoreCase("post", method)) {
            throw new Exception("The only valid method for this action is POST.");
        }

        final String submittedCsrf = data.getParameters().getString("XNAT_CSRF");
        final String sessionCsrf = (String) data.getSession().getAttribute("XNAT_CSRF");
        if (!StringUtils.equals(submittedCsrf, sessionCsrf)) {
            final String errorMessage = SecureAction.csrfTokenErrorMessage(data.getRequest());
            AdminUtils.sendAdminEmail("Possible phishing or intrusion Attempt", "The XNAT_CSRF token was not properly set in the session when someone tried to change a password.\n" + errorMessage);
            throw new Exception("INVALID CSRF (" + errorMessage + ")");
        }

        String username = TurbineUtils.getUser(data).getUsername();
        String password = data.getParameters().getString(CGI_PASSWORD);
        if ((username != null) && !StringUtils.isEmpty(username)) {
            if (!username.equals("guest")) {
                if (StringUtils.isEmpty(username)) {
                    return;
                } else {
                    if (username.contains("/")) {
                        username = username.substring(username.lastIndexOf("/") + 1);
                    }
                    if (username.contains("\\")) {
                        username = username.substring(username.lastIndexOf("\\") + 1);
                    }
                }

                try {
                    UserI oldUser = TurbineUtils.getUser(data);

                    if (!oldUser.isEnabled()) {
                        throw new Exception("User is not enabled: " + oldUser.getUsername());
                    }
                    if (XDAT.verificationOn() && !oldUser.isVerified()) {
                        throw new Exception("User is not verified: " + oldUser.getUsername());
                    }

                    UserI existing = Users.getUser(oldUser.getID());
                    existing.setPassword(password);
                    try {
                        Users.save(oldUser, existing, false, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Modified user password"));
                    } catch (Exception e) {
                        invalidInformation(data, context, e.getMessage());
                        logger.error("Error Storing User", e);
                        return;
                    }
                    data.getSession().setAttribute("expired", false);
                    data.getSession().setAttribute("forcePasswordChange", false);
                    data.setMessage("Password changed.");

                } catch (Exception e) {
                    log.error("", e);

                    AccessLogger.LogActionAccess(data, "Failed Login by '" + username + "': " + e.getMessage());

                    if (username.toLowerCase().contains("script")) {
                        e = new Exception("Illegal username &lt;script&gt; usage.");
                        AdminUtils.sendAdminEmail("Possible Cross-site scripting attempt blocked", StringEscapeUtils.escapeHtml(username));
                        logger.error("", e);
                        data.setScreenTemplate("Error.vm");
                        data.getParameters().setString("exception", e.toString());
                        return;
                    }

                    // Set Error Message and clean out the user.
                    if (e instanceof SQLException) {
                        data.setMessage("An error has occurred.  Please contact a site administrator for assistance.");
                    } else {
                        data.setMessage(e.getMessage());
                    }

                    String loginTemplate = org.apache.turbine.Turbine.getConfiguration().getString("template.login");

                    if (StringUtils.isNotEmpty(loginTemplate)) {
                        // We're running in a templating solution
                        data.setScreenTemplate(loginTemplate);
                    } else {
                        data.setScreen(org.apache.turbine.Turbine.getConfiguration().getString("screen.login"));
                    }
                }
            } else {
                invalidInformation(data, context, "Guest account password must be managed in the administration section.");
            }
        } else {
            invalidInformation(data, context, "You must must be authenticated or have a token to change this password.");
        }
    }

    public void invalidInformation(RunData data, Context context, String message) {
        try {
            String nextPage = (String) TurbineUtils.GetPassedParameter("nextPage", data);
            String nextAction = (String) TurbineUtils.GetPassedParameter("nextAction", data);
            String par = (String) TurbineUtils.GetPassedParameter("par", data);

            if (!StringUtils.isEmpty(par)) {
                context.put("par", par);
            }
            if (!StringUtils.isEmpty(nextAction) && !nextAction.contains("XDATLoginUser") && !nextAction.equals(org.apache.turbine.Turbine.getConfiguration().getString("action.login"))) {
                context.put("nextAction", nextAction);
            } else if (!StringUtils.isEmpty(nextPage) && !nextPage.equals(org.apache.turbine.Turbine.getConfiguration().getString("template.home"))) {
                context.put("nextPage", nextPage);
            }
            data.setMessage(message);
        } catch (Exception e) {
            logger.error(message, e);
            data.setMessage(message);
        } finally {
            data.setScreenTemplate("ChangePassword.vm");
        }
    }

    @Override
    protected boolean isAuthorized(RunData data) throws Exception {
        return true;
    }

    private static final Logger logger = Logger.getLogger(XDATChangePassword.class);
}
       
