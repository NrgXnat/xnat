// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xdat.turbine.modules.screens;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import javax.mail.MessagingException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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
    		if(storeParameterIfPresent(data, context, "emailTo") && storeParameterIfPresent(data, context, "emailUsername")){
    			String emailTo = (String)context.get("emailTo");
        		String emailUsername = (String)context.get("emailUsername");
        		XDATUser user = new XDATUser(emailUsername);
        		if(!user.isVerified()){
	        		String subject = TurbineUtils.GetSystemName() + " Email Verification";
	        		String admin = AdminUtils.getAdminEmailId();        		
	    	        AliasToken token = XDAT.getContextService().getBean(AliasTokenService.class).issueTokenForUser(user,true,null);
	    	        
	    	        String text = "Dear " + user.getFirstname() + " " + user.getLastname() + ",<br/>\r\n" + "Please click this link to verify your email address: " + TurbineUtils.GetFullServerPath() + "/app/template/VerifyEmail.vm?a=" + token.getAlias() + "&s=" + token.getSecret() + "<br/>\r\nThis link will expire in 24 hours.";
	    	        XDAT.getMailService().sendHtmlMessage(admin, emailTo, subject, text);
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
