// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xdat.turbine.modules.screens;

import java.sql.SQLException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.services.AliasTokenService;

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
            if (data != null && TurbineUtils.getUser(data) != null &&
                    !StringUtils.isBlank(TurbineUtils.getUser(data).getUsername()) &&
                    !TurbineUtils.getUser(data).getUsername().equalsIgnoreCase("guest")) {
                context.put("login", TurbineUtils.getUser(data).getUsername());
                context.put("topMessage", "Your password has expired. Please choose a new one.");
            } else {
                String alias = (String) TurbineUtils.GetPassedParameter("a", data);
                String secret = (String) TurbineUtils.GetPassedParameter("s", data);
                
                if(alias!=null && secret!=null){
                	String userID="";
            		try
            		{
            			userID = XDAT.getContextService().getBean(AliasTokenService.class).validateToken(alias,Long.parseLong(secret));
            	    	if(userID!=null){
            	    		XDATUser user = new XDATUser(userID);
            	    		boolean forcePasswordChange = true;
            	    		XDAT.loginUser(data, user, forcePasswordChange);
            	    	}
            	    	else{
            	        	invalidInformation(data, context, "Invalid token.");
            	        }

            		}
            		catch (Exception e)
            		{
                        log.error("",e);

                        AccessLogger.LogActionAccess(data, "Failed Login by alias '" + alias +"': " +e.getMessage());
                        
                        if(userID.toLowerCase().contains("script"))
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
                
                context.put("topMessage", "Please choose a new password.");
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
    			if (!StringUtils.isEmpty(nextAction) && nextAction.indexOf("XDATLoginUser")==-1 && !nextAction.equals(org.apache.turbine.Turbine.getConfiguration().getString("action.login"))){
    				context.put("nextAction", nextAction);
    			}else if (!StringUtils.isEmpty(nextPage) && !nextPage.equals(org.apache.turbine.Turbine.getConfiguration().getString("template.home")) ) {
    				context.put("nextPage", nextPage);
    			}
    			data.setMessage(message);
    		} catch (Exception e) {
              log.error(message,e);
    			data.setMessage(message);
    		}finally{
    			data.setScreenTemplate("ChangePassword.vm");
    		}
      }
}
