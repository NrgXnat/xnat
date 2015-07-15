// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xdat.turbine.modules.screens;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;

import java.sql.SQLException;

@SuppressWarnings("UnusedDeclaration")
public class ChangePassword extends VelocitySecureScreen {

    @Override
    protected void doBuildTemplate(RunData data) throws Exception {
        Context c = TurbineVelocity.getContext(data);
        SecureScreen.loadAdditionalVariables(data, c);
        doBuildTemplate(data, c);
    }

    @Override
    protected void doBuildTemplate(RunData data, Context context) {
        try {
            XDATUser user;
            if (TurbineUtils.getUser(data) != null) {
                user = TurbineUtils.getUser(data);
                if (!StringUtils.isBlank(user.getUsername()) &&
                    !user.getUsername().equalsIgnoreCase("guest") &&
                    !TurbineUtils.HasPassedParameter("a", data) && !TurbineUtils.HasPassedParameter("s", data)) {
                    context.put("login", user.getUsername());
                    context.put("topMessage", "Your password has expired. Please choose a new one.");
                }
            } else {
                user = (XDATUser) data.getSession().getAttribute("user");

                // If the user isn't already logged in...
                if(user == null || user.getUsername().equals("guest")) {
                    String alias = (String) TurbineUtils.GetPassedParameter("a", data);
                    String secret = (String) TurbineUtils.GetPassedParameter("s", data);

                    if(alias != null && secret != null) {
                        String userID = "";
                        try
                        {
                            userID = XDAT.getContextService().getBean(AliasTokenService.class).validateToken(alias,Long.parseLong(secret));
                            if(userID!=null){
                                user = new XDATUser(userID);
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

                            AccessLogger.LogActionAccess(data, "Failed Login by alias '" + alias +"': " +e.getMessage());

                            if(StringUtils.equalsIgnoreCase(userID, "script"))
                            {
                                e= new Exception("Illegal username &lt;script&gt; usage.");
                                AdminUtils.sendAdminEmail("Possible Cross-site scripting attempt blocked", StringEscapeUtils.escapeHtml(userID));
                                log.error("",e);
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
            log.error(message, e);
            data.setMessage(message);
        } finally {
            data.setScreenTemplate("ChangePassword.vm");
        }
    }
}
