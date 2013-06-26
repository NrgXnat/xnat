// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xdat.turbine.modules.screens;

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
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;

import java.sql.SQLException;

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
	    		XDATUser user = new XDATUser(userID);
	    		XFTItem toSave = XFTItem.NewItem("xdat:user", user);
                toSave.setProperty("login", user.getLogin());
                toSave.setProperty("primary_password", user.getProperty("primary_password"));
                toSave.setProperty("email", user.getProperty("email"));
                toSave.setProperty("verified", "1");
                // If auto-approval is true, the user is enabled
				if(XFT.GetUserRegistration()){
					toSave.setProperty("enabled", "1");
				}
				try {
					XDATUser.ModifyUser(user, toSave, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Verify User Email"));
					
					//After their email is verified, if auto-approval is false, the admin should get a notification
					if(!XFT.GetUserRegistration()){
						try{
                            UserRegistrationData regData = XDAT.getContextService().getBean(UserRegistrationDataService.class).getUserRegistrationData(user);
                            String phone = regData.getPhone();
                            String organization = regData.getOrganization();
                            String comments = regData.getComments();
		                    data.setMessage("Your email has been verified.");
				    		AdminUtils.sendNewUserRequestNotification(user.getUsername(), user.getFirstname(), user.getLastname(), user.getEmail(), comments, phone, organization, context);
				        } catch (Exception exception) {
				            logger.error("Error occurred sending new user request email", exception);
				        } finally {
				            data.setRedirectURI(null);
				            data.setScreenTemplate("PostRegister.vm");
				        }
					}
					
				} catch (Exception e) {
					invalidInformation(data, context, e.getMessage());
					logger.error("Error Verifying User", e);
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
            
			String loginTemplate =  org.apache.turbine.Turbine.getConfiguration().getString("template.login");

			if (StringUtils.isNotEmpty(loginTemplate)) {
				// We're running in a templating solution
				data.setScreenTemplate(loginTemplate);
			} else {
				data.setScreen(Turbine.getConfiguration().getString("screen.login"));
			}
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
}
