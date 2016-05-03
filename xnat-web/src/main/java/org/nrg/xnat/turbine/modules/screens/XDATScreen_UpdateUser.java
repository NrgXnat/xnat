/*
 * org.nrg.xnat.turbine.modules.screens.Search
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.screens;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;

import java.sql.SQLException;

public class XDATScreen_UpdateUser extends VelocitySecureScreen {

    @Override
    protected void doBuildTemplate(RunData data) throws Exception {
        Context c = TurbineVelocity.getContext(data);
        doBuildTemplate(data, c);
    }

    protected void doBuildTemplate(RunData data, Context context) throws Exception {

        try {
            UserI user = TurbineUtils.getUser(data);
            if (user != null && !user.getUsername().equalsIgnoreCase("guest")) {
                user = TurbineUtils.getUser(data);
                if (!StringUtils.isBlank(user.getUsername()) &&
                        !TurbineUtils.HasPassedParameter("a", data) && !TurbineUtils.HasPassedParameter("s", data)) {
                    if(data.getSession().getAttribute("expired")!=null && (Boolean)data.getSession().getAttribute("expired")) {
                        context.put("topMessage", "Your password has expired. Please choose a new one.");
                        context.put("expired", true);
                    }
                    context.put("login", user.getUsername());
                    context.put("item", user);
                }
            } else {
                user = XDAT.getUserDetails();

                // If the user isn't already logged in...
                if(user == null || user.getUsername().equals("guest")) {
                    String alias = (String) TurbineUtils.GetPassedParameter("a", data);
                    String secret = (String) TurbineUtils.GetPassedParameter("s", data);

                    if(alias != null && secret != null) {
                        String userID="";
                        try
                        {
                            context.put("forgot", true);
                            data.getSession().setAttribute("forgot", true);
                            userID = XDAT.getContextService().getBean(AliasTokenService.class).validateToken(alias,Long.parseLong(secret));
                            if(userID!=null){
                                user = Users.getUser(userID);
                                XDAT.loginUser(data, user, true);
                            }
                            else{
                                invalidInformation(data, context, "Change password opportunity expired.  Change password requests can only be used once and expire after 24 hours.  Please restart the change password process.");
                                context.put("hideChangePasswordForm", true);
                            }
                        }
                        catch (Exception e)
                        {
                            log.error("",e);

                            AccessLogger.LogActionAccess(data, "Failed Login by alias '" + alias + "': " + e.getMessage());

                            if (userID == null || userID.toLowerCase().contains("script")) {
                                e = new Exception("Illegal username &lt;script&gt; usage.");
                                final String message = userID == null ? "No user ID found." : StringEscapeUtils.escapeHtml4(userID);
                                AdminUtils.sendAdminEmail("Possible Cross-site scripting attempt blocked", message);
                                log.error(message,e);
                                data.setScreenTemplate("Error.vm");
                                data.getParameters().setString("exception", e.toString());
                                return;
                            }

                            // Set Error Message and clean out the user.
                            if(e instanceof SQLException){
                                data.setMessage("An error has occurred.  Please contact a site administrator for assistance.");
                            }else{
                                data.setMessage(e.getMessage());
                            }
                        }
                    }
                }

                if(user != null){
                    context.put("item", user);
                    if(!user.isEnabled()) {
                        throw new Exception("User is not enabled: " + user.getUsername());
                    }
                    if (XDAT.verificationOn() && !user.isVerified()) {
                        throw new Exception("User is not verified: " + user.getUsername());
                    }
                }

                context.put("topMessage", "Enter a new password.");

            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    @Override
    protected boolean isAuthorized(RunData arg0) throws Exception {
        return false;
    }

    public void invalidInformation(RunData data,Context context, String message){
        try {
            String nextPage = (String)TurbineUtils.GetPassedParameter("nextPage",data);
            String nextAction = (String)TurbineUtils.GetPassedParameter("nextAction",data);
            String par = (String)TurbineUtils.GetPassedParameter("par",data);

            if(!StringUtils.isEmpty(par)){
                context.put("par", par);
            }
            if (!StringUtils.isEmpty(nextAction) && !nextAction.contains("XDATLoginUser") && !nextAction.equals(org.apache.turbine.Turbine.getConfiguration().getString("action.login"))) {
                context.put("nextAction", nextAction);
            }else if (!StringUtils.isEmpty(nextPage) && !nextPage.equals(org.apache.turbine.Turbine.getConfiguration().getString("template.home")) ) {
                context.put("nextPage", nextPage);
            }
            data.setMessage(message);
        } catch (Exception e) {
            log.error(message,e);
            data.setMessage(message);
        }finally{
            data.setScreenTemplate("XDATScreen_UpdateUser.vm");
        }
    }

    static org.apache.log4j.Logger logger = Logger.getLogger(XDATScreen_UpdateUser.class);

}
