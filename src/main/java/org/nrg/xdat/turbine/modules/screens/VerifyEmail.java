// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xdat.turbine.modules.screens;

import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.Turbine;
import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.UserRegistrationData;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.services.UserRegistrationDataService;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.search.ItemSearch;

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
    	String alias = "";

        try {
            if (XFT.GetUserRegistration()) {
                context.put("autoApproval", "true");
            } else {
                context.put("autoApproval", "false");
            }

            alias = (String) TurbineUtils.GetPassedParameter("a", data);
            String secret = (String) TurbineUtils.GetPassedParameter("s", data);
			String userID = XDAT.getContextService().getBean(AliasTokenService.class).validateToken(alias, Long.parseLong(secret));
	    	if (userID!=null) {
	    		XDATUser u = new XDATUser(userID);
	    		ItemCollection users = getAllUsersWithEmail(u.getEmail());
	    		ArrayList<XDATUser> verified = new ArrayList<XDATUser>();

	    		for(ItemI i : users.getItems()){
	    			XDATUser curUser = new XDATUser(i.getItem(),false);
	    			if(!curUser.getVerified()){
	    				XFTItem toSave = XFTItem.NewItem("xdat:user", curUser);
	    				toSave.setProperty("login", curUser.getLogin());
	    				toSave.setProperty("primary_password", curUser.getProperty("primary_password"));
	    				toSave.setProperty("email", curUser.getProperty("email"));
	    				toSave.setProperty("verified", "1");
	    			
	    				// If auto-approval is true, the user is enabled
	    				if(XFT.GetUserRegistration()){
	    					toSave.setProperty("enabled", "1");
	    				}
	    				
	    				try {
	    					// Save the user, and add the user to the list of verified users.
	    					XDATUser.ModifyUser(curUser, toSave, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Verify User Email"));
	    					verified.add(curUser);
	    				} catch (Exception e) {
	    					invalidInformation(data, context, e.getMessage());
	    					logger.error("Error Verifying User", e);
	    				}
	    			}
	    		}

                // Default message if we didn't verify any users. (Changed below if verified.size > 0)
                String userMessage = "All users with the email, " + u.getEmail() + ", have been previously verified.";

                try{

					// If we verified any of the above users.
					if(verified.size() > 0){
						// Build the user message
						StringBuilder msgBuilder = new StringBuilder();
						msgBuilder.append(u.getEmail()).append(" has been verified for the following users: ");
						for(XDATUser uv : verified){ 
							// Append a list of user names that we have verified.
							if(verified.get(verified.size() - 1) == uv){
								msgBuilder.append(uv.getUsername()); // Don't append comma if it's the last in the list.
							}else{
								msgBuilder.append(uv.getUsername()).append(", ");
							}
							
							//If auto approval is false, send a notification to the administrator for each user we just verified.
							if(!XFT.GetUserRegistration()){
								// Get phone, organization, and comments from the users registration data
								UserRegistrationData regData = XDAT.getContextService().getBean(UserRegistrationDataService.class).getUserRegistrationData(uv);
								String comments = "";
								String phone = "";
								String organization = "";
								
								// regData will be null if the user was created by an admin (via admin > users > add user)
								if(null != regData){ 
									phone = regData.getPhone();
									organization = regData.getOrganization();
									comments = regData.getComments();
								}
							
								// Send admin email
								AdminUtils.sendNewUserRequestNotification(uv.getUsername(), uv.getFirstname(), uv.getLastname(), uv.getEmail(), comments, phone, organization, context);
							}
						}
						// Set the user message.
						userMessage = msgBuilder.toString();
					}
				} 
				catch (Exception exception) {
					logger.error("Error occurred sending admin email to enable newly verified accounts", exception);
				}
                if(!XFT.GetUserRegistration()){
                    //data.setRedirectURI(null);
                    data.setMessage("Thank you for your interest in our site. Your user account will be reviewed and enabled by the site administrator. When this is complete, you will receive an email inviting you to login to the site.");
                    redirectToLogin(data);
				}
                else{
                    // Set message to display to the user. You do not need a message informing you of the accounts that were verified if all you did was register and you did not click a verify email link.
                    //data.setRedirectURI(null);
                    data.setMessage(userMessage);
                    redirectToLogin(data);
                }
	    	} else {
	        	invalidInformation(data, context, "Invalid token. Your email could not be verified.");
	        }
		} catch (Exception e) {
            logger.error("",e);

            AccessLogger.LogActionAccess(data, "Failed Login by alias '" + alias +"': " +e.getMessage());

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
    
    /**
     * Function looks up all users with the given email.
     * @param String email - the email we are searching on. 
     * @return ItemCollection containing all users with the given email 
     */
    private ItemCollection getAllUsersWithEmail(String email) throws Exception{
        ItemSearch search = new ItemSearch();
        search.setAllowMultiples(false);
        search.setElement("xdat:user");
        search.addCriteria("xdat:user.email",email);
        return search.exec();
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
