/*
 * core: org.nrg.xdat.turbine.modules.screens.VerifyEmail
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.screens;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.Turbine;
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
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class VerifyEmail extends VelocitySecureScreen {
	private static final Logger logger = Logger.getLogger(VerifyEmail.class);
	
    @Override
    protected void doBuildTemplate(RunData data) throws Exception {
        Context c = TurbineVelocity.getContext(data);
        SecureScreen.loadAdditionalVariables(data, c);
        doBuildTemplate(data, c);
    }

    @Override
    protected void doBuildTemplate(final RunData data, final Context context) {
        final String alias = (String) TurbineUtils.GetPassedParameter("a", data);
        final String secret = (String) TurbineUtils.GetPassedParameter("s", data);
        final String userID = XDAT.getContextService().getBean(AliasTokenService.class).validateToken(alias, secret);

        try {
            if (StringUtils.isNotBlank(userID)) {
                final UserI user = Users.getUser(userID);
                final List<UserI> users = getAllUsersWithEmail(user.getEmail());
                final List<UserI> verified = new ArrayList<>();

                final boolean autoApproveRegistered = XDAT.getSiteConfigPreferences().getUserRegistration();

                for (final UserI current : users) {
                    if ((current.isVerified() == null || !current.isVerified()) || (!current.isEnabled() && disabledDueToInactivity(current))) {
                        current.setVerified(true);
                        verified.add(current);

                        // If auto-approval is true, the user is enabled
                        if (autoApproveRegistered) {
                            current.setEnabled(true);
                        }

                        try {
                            // Save the user, and add the user to the list of verified users.
                            // need to specify override security because users generally cannot enable their own account.
                            Users.save(current, current, true, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Verify User Email"));
                        } catch (Exception e) {
                            invalidInformation(data, context, e.getMessage());
                            logger.error("Error Verifying User", e);
                        }
                    }
                }

                final String message;
                try {
                    // If we verified any of the above users.
                    if (verified.size() > 0) {
                        // Build the user message
                        final StringBuilder buffer = new StringBuilder();
                        buffer.append(user.getEmail()).append(" has been verified for the following users: ");
                        for (final UserI current : verified) {
                            // Append a list of user names that we have verified.
                            if (verified.get(verified.size() - 1) == current) {
                                buffer.append(current.getUsername()); // Don't append comma if it's the last in the list.
                            } else {
                                buffer.append(current.getUsername()).append(", ");
                            }

                            // If this user has never logged in, they're new, send the appropriate notification.
                            if (Users.getLastLogin(current) == null) {
                                AdminUtils.sendNewUserNotification(current, context);
                            } else if (disabledDueToInactivity(current)) {
                                AdminUtils.sendDisabledUserVerificationNotification(current, context);
                            }
                        }
                        // Set the user message.
                        message = buffer.toString();
                    } else {
                        message = "All users with email address " + user.getEmail() + " have been previously verified.";
                    }
                    if (!autoApproveRegistered) {
                        //data.setRedirectURI(null);
                        data.setMessage("Thank you for your interest in our site. Your user account will be reviewed and enabled by the site administrator. When this is complete, you will receive an email inviting you to login to the site.");
                        redirectToLogin(data);
                    } else {
                        // Set message to display to the user. You do not need a message informing you of the accounts that were verified if all you did was register and you did not click a verify email link.
                        //data.setRedirectURI(null);
                        data.setMessage(message);
                        redirectToLogin(data);
                    }
                } catch (Exception exception) {
                    logger.error("Error occurred sending admin email to enable newly verified accounts", exception);
                }
            } else {
                invalidInformation(data, context, "Invalid token. Your email could not be verified.");
            }
        } catch (Exception e) {
            logger.error("", e);

            AccessLogger.LogActionAccess(data, "Failed Login by alias '" + alias + "': " + e.getMessage());

            // Set Error Message and clean out the user.
            if (e instanceof SQLException) {
                data.setMessage("An error has occurred.  Please contact a site administrator for assistance.");
            } else {
                data.setMessage(e.getMessage());
            }

            redirectToLogin(data);
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
    			if (!StringUtils.isEmpty(nextAction) && !nextAction.contains("XDATLoginUser") && !nextAction.equals(Turbine.getConfiguration().getString("action.login"))){
    				context.put("nextAction", nextAction);
    			}else if (!StringUtils.isEmpty(nextPage) && !nextPage.equals(Turbine.getConfiguration().getString("template.home")) ) {
    				context.put("nextPage", nextPage);
    			}
    			data.setMessage(message);
    		} catch (Exception e) {
              logger.error(message,e);
    			data.setMessage(message);
    		}finally{
    			data.setScreen(Turbine.getConfiguration().getString("screen.login"));
    		}
      }

    private boolean disabledDueToInactivity(UserI user) {
        try {
            String query = "SELECT COUNT(*) AS count " +
                    "FROM xdat_user_history " +
                    "WHERE xdat_user_id=" + user.getID() + " " +
                    "AND change_user=" + user.getID() + " " +
                    "AND change_date = (SELECT MAX(change_date) " +
                    "FROM xdat_user_history " +
                    "WHERE xdat_user_id=" + user.getID() + " " +
                    "AND enabled=1)";
            Long result = (Long) PoolDBUtils.ReturnStatisticQuery(query, "count", PoolDBUtils.getDefaultDBName(), null);

            return result > 0;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return false;
    }
    
    /**
     * Function looks up all users with the given email.
     * @param email    The email we are searching on.
     * @return ItemCollection containing all users with the given email 
     */
    private List<UserI> getAllUsersWithEmail(String email) throws Exception{
        return Users.getUsersByEmail(email);
     }

    private void redirectToLogin(final RunData data){
        String loginTemplate =  org.apache.turbine.Turbine.getConfiguration().getString("template.login");

        if (StringUtils.isNotEmpty(loginTemplate)) {
            // We're running in a templating solution
            data.setScreenTemplate(loginTemplate);
        } else {
            data.setScreen(Turbine.getConfiguration().getString("screen.login"));
        }
    }
}
