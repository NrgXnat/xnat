/*
 * core: org.nrg.xdat.turbine.modules.actions.XDATForgotLogin
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.actions;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.modules.actions.VelocitySecureAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.mail.services.EmailRequestLogService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;

import javax.mail.MessagingException;
import java.util.Date;
import java.util.List;

public class XDATForgotLogin extends VelocitySecureAction {
    static Logger logger = Logger.getLogger(XDATForgotLogin.class);
    
    private final EmailRequestLogService requestLog = XDAT.getContextService().getBean(EmailRequestLogService.class);

    public void additionalProcessing(RunData data, Context context,UserI user) throws Exception{
    	
    }
    
    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        //noinspection Duplicates
        try {
			SecureAction.isCsrfTokenOk(data);
		} catch (Exception e1) {
			data.setMessage("Due to a technical issue, the requested action cannot be performed.");
			data.setScreenTemplate("Login.vm");
			return;
		}
    	
        String email = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("email",data));
        String username = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("username",data));
		String subject = TurbineUtils.GetSystemName() + " Login Request";
		String admin = XDAT.getSiteConfigPreferences().getAdminEmail();
		if (!StringUtils.isBlank(email)) {
            //check email
			
			List<? extends UserI> users=Users.getUsersByEmail(email);
			
            if (users==null || users.size()==0){
                data.setMessage("Unknown email address.");
                data.setScreenTemplate("ForgotLogin.vm");
            }else{
            	UserI user =users.get(0);
                
                try{
					additionalProcessing(data, context, user);
                }catch(Exception e){
                    logger.error(e);
                }
            	
                try {
                	if(requestLog != null && requestLog.isEmailBlocked(email)){
                		data.setMessage("You have exceeded the allowed number of email requests. Please try again later.");
                		data.setScreenTemplate("Login.vm");
                	}else{
                		String url=TurbineUtils.GetFullServerPath() + "/app/template/Index.vm";
                    	String message = XDAT.getNotificationsPreferences().getEmailMessageForgotUsernameRequest();
                        message = message.replaceAll("USER_USERNAME",user.getUsername());
                        message = message.replaceAll("SITE_URL",url);
                        message = message.replaceAll("SITE_NAME",TurbineUtils.GetSystemName());
                        message = message.replaceAll("USER_FIRSTNAME",user.getFirstname());
                        message = message.replaceAll("USER_LASTNAME",user.getLastname());
                        message = message.replaceAll("ADMIN_EMAIL",XDAT.getSiteConfigPreferences().getAdminEmail());
                        message = message.replaceAll("HELP_EMAIL",XDAT.getNotificationsPreferences().getHelpContactInfo());


                    	XDAT.getMailService().sendHtmlMessage(admin, email, subject, message);
                    	if(requestLog!= null){ requestLog.logEmailRequest(email, new Date()); }
						data.setMessage("The corresponding username for this email address has been emailed to your account.");
						data.setScreenTemplate("Login.vm");
                	}
				} catch (MessagingException exception) {
					logger.error(exception);
					data.setMessage("Due to a technical difficulty, we are unable to send you the email containing your information.  Please contact our technical support.");
					data.setScreenTemplate("ForgotLogin.vm");
                }
            }
		} else if (!StringUtils.isBlank(username)) {
            //check user
                UserI user=null;
				try {
					user = Users.getUser(username);
				} catch (Exception ignored) {
				}

                if (user==null){
                    data.setMessage("Unknown username.");
                    data.setScreenTemplate("ForgotLogin.vm");
                }else{
                    try{
                    	additionalProcessing(data, context, user);
                    }catch(Exception e){
                        logger.error(e);
                    }

                    // If the user is enabled, go ahead and do this stuff.
                    if (user.isEnabled()) {
                        try {
                           String to = user.getEmail();
                           if(requestLog != null && requestLog.isEmailBlocked(to)){
                               data.setMessage("You have exceeded the allowed number of email requests. Please try again later.");
                               data.setScreenTemplate("Login.vm");
                            }else{
                               AliasToken token = XDAT.getContextService().getBean(AliasTokenService.class).issueTokenForUser(user,true,null);

                               String text = XDAT.getNotificationsPreferences().getEmailMessageForgotPasswordReset();
                               text=text.replaceAll("USER_USERNAME",user.getUsername());
                               text=text.replaceAll("USER_FIRSTNAME",user.getFirstname());
                               text=text.replaceAll("USER_LASTNAME",user.getLastname());
                               text=text.replaceAll("ADMIN_EMAIL",XDAT.getSiteConfigPreferences().getAdminEmail());
                               text=text.replaceAll("HELP_EMAIL",XDAT.getNotificationsPreferences().getHelpContactInfo());
                               text=text.replaceAll("RESET_URL",TurbineUtils.GetFullServerPath() + "/app/template/XDATScreen_UpdateUser.vm?a=" + token.getAlias() + "&s=" + token.getSecret());
                               text=text.replaceAll("SITE_URL",TurbineUtils.GetFullServerPath());
                               text=text.replaceAll("SITE_NAME",TurbineUtils.GetSystemName());

                               XDAT.getMailService().sendHtmlMessage(admin, to, subject, text);
                               if(requestLog != null){ requestLog.logEmailRequest(to, new Date()); }
                               data.setMessage("You have been sent an email with a link to reset your password. Please check your email.");
                               data.setScreenTemplate("Login.vm");
                            }
                        } catch (MessagingException e) {
                            logger.error("Unable to send mail",e);
                            System.out.println("Error sending Email");

                            data.setMessage("Due to a technical difficulty, we are unable to send you the email containing your information.  Please contact our technical support.");
                            data.setScreenTemplate("ForgotLogin.vm");
                        }
                    } else {
                        // If the user is NOT enabled, notify administrator(s).
                        final String message = "Disabled user attempted to reset password: " + user.getUsername();
                        logger.warn(message);
                        AdminUtils.sendAdminEmail(user, "Possible hack attempt", "Someone attempted reset the password for the account " + user.getUsername() + ", but this account is currently disabled. You can contact the registered account owner through the email address: " + user.getEmail() + ".");
                        data.setMessage("Your account is currently disabled. Please contact the system administrator.");
                        data.setScreenTemplate("Login.vm");
                    }
                }
            }else{
                data.setScreenTemplate("ForgotLogin.vm");
            }
        }

    @Override
    protected boolean isAuthorized(RunData data) throws Exception {
        return true;
    }

}
