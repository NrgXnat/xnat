// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xdat.turbine.modules.screens;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import javax.mail.MessagingException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.mail.services.EmailRequestLogService;


public class ResendVerification extends SecureScreen {
	static Logger logger = Logger.getLogger(ResendVerification.class);
	
    @Override
    protected void doBuildTemplate(RunData data) throws Exception {
        Context c = TurbineVelocity.getContext(data);
        SecureScreen.loadAdditionalVariables(data, c);
        doBuildTemplate(data, c);
    }

    @Override
    protected void doBuildTemplate(final RunData data, final Context context) {
    	try {
    		EmailRequestLogService requestLog = XDAT.getContextService().getBean(EmailRequestLogService.class);
    		if(storeParameterIfPresent(data, context, "emailTo") && storeParameterIfPresent(data, context, "emailUsername")){
    			String emailTo = (String)context.get("emailTo");
        		String emailUsername = (String)context.get("emailUsername");
        		XDATUser user = new XDATUser(emailUsername);
        		if(!user.isVerified()){
                    if(requestLog != null && requestLog.isEmailBlocked(emailTo)){
                        data.setMessage("You have exceeded the allowed number of email requests. Please try again later.");
                        data.setScreenTemplate("Login.vm");
                    }else{
                        AdminUtils.sendNewUserVerificationEmail(user);
            			if(requestLog != null){ requestLog.logEmailRequest(emailTo, new Date()); }
                    }
        		}
        		else{ 
        			throw new MessagingException(); 
        		}
        	}
    		else{ 
    			throw new MessagingException(); 
    		}
    	 } catch (Exception e) {
    		 data.setMessage("Due to a technical difficulty, we are unable to resend the verification email. Please contact our technical support.");
             logger.error("Error Resending Email Verification",e);
             data.setRedirectURI(null);
             data.setScreenTemplate("Login.vm");
         }
        
    }

    @Override
    protected boolean isAuthorized(RunData arg0) throws Exception {
        return false;
    }
  
}
