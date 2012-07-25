//Copyright Washington University School of Medicine All Rights Reserved
/*
 * Created on Dec 12, 2006
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import javax.mail.MessagingException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.modules.actions.VelocitySecureAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.services.AliasTokenService;
import javax.mail.MessagingException;

public class XDATForgotLogin extends VelocitySecureAction {
    static Logger logger = Logger.getLogger(XDATForgotLogin.class);

    public void additionalProcessing(RunData data, Context context,XDATUser user) throws Exception{
    	
    }
    
    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        String email = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("email",data));
        String username = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("username",data));
		String subject = TurbineUtils.GetSystemName() + " Login Request";
		String admin = AdminUtils.getAdminEmailId();
		if (!StringUtils.isBlank(email)) {
            //check email
            ItemSearch search = new ItemSearch();
            search.setAllowMultiples(false);
            search.setElement("xdat:user");
            search.addCriteria("xdat:user.email",email);

            ItemI temp = search.exec().getFirst();
            if (temp==null){
                data.setMessage("Unknown email address.");
                data.setScreenTemplate("ForgotLogin.vm");
                return;
            }else{
				XDATUser user = new XDATUser(temp, false);
                
                try{
					additionalProcessing(data, context, user);
                }catch(Exception e){
                    logger.error(e);
                }
            	
                try {
                	String url=TurbineUtils.GetFullServerPath() + "/app/template/Index.vm";
                    String message = String.format(USERNAME_REQUEST, user.getUsername(), url, TurbineUtils.GetSystemName());
                    XDAT.getMailService().sendHtmlMessage(admin, email, subject, message);
					data.setMessage("The corresponding username for this email address has been emailed to your account.");
					data.setScreenTemplate("Login.vm");
				} catch (MessagingException exception) {
					logger.error(exception);
					data.setMessage("Due to a technical difficulty, we are unable to send you the email containing your information.  Please contact our technical support.");
					data.setScreenTemplate("ForgotLogin.vm");
					return;
                }
            }
		} else if (!StringUtils.isBlank(username)) {
            //check user
                ItemSearch search = new ItemSearch();
                search.setAllowMultiples(false);
                search.setElement("xdat:user");
                search.addCriteria("xdat:user.login",username);

                ItemI temp = search.exec().getFirst();
                if (temp==null){
                    data.setMessage("Unknown username.");
                    data.setScreenTemplate("ForgotLogin.vm");
                    return;
                }else{
                	XDATUser user = new XDATUser(temp, false);

                    try{
                    	additionalProcessing(data, context, user);
                    }catch(Exception e){
                        logger.error(e);
                    }
                	
                    String newPassword = XFT.CreateRandomAlphaNumeric(10);
					if (user.getBooleanProperty("primary_password.encrypt", true)) {
						// This tempPass was never used, but wanted to make sure I wasn't missing something...
						// String tempPass = newUser.getStringProperty("primary_password");
						user.setProperty("primary_password", XDATUser.EncryptString(newPassword));
                    }
                    
                    try {
                    	String to = user.getEmail();
				        String url=TurbineUtils.GetFullServerPath() + "/app/action/XDATActionRouter/xdataction/MyXNAT";
				        AliasToken token = XDAT.getContextService().getBean(AliasTokenService.class).issueTokenForUser(user,true,null);
				        String text = "Dear " + user.getFirstname() + " " + user.getLastname() + ",<br/>\r\n" + "Please click this link to reset your password: " + TurbineUtils.GetFullServerPath() + "/app/template/ChangePassword.vm?a=" + token.getAlias() + "&s=" + token.getSecret() + "<br/>\r\nThis link will expire in 24 hours.";
				        XDAT.getMailService().sendHtmlMessage(admin, to, subject, text);
				        data.setMessage("You have been sent an email with a link to reset your password. Please check your email.");
				        data.setScreenTemplate("Login.vm");
                    } catch (MessagingException e) {
                        logger.error("Unable to send mail",e);
                        System.out.println("Error sending Email");

                        data.setMessage("Due to a technical difficulty, we are unable to send you the email containing your information.  Please contact our technical support.");
                        data.setScreenTemplate("ForgotLogin.vm");
                        return;
                    }
					
                    //if it can't send an email, then it should save the modified user password.
					SaveItemHelper.authorizedSave(user,null, true, false,EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Password Reset"));
					
                }
            }else{
                data.setScreenTemplate("ForgotLogin.vm");
                return;
            }
        }

    @Override
    protected boolean isAuthorized(RunData data) throws Exception {
        return true;
    }

	// TODO: This should be converted to use a Velocity template or property in
	// a resource bundle.
	private static final String USERNAME_REQUEST = "<html><body>\nYou requested your username, which is: %s\n<br><br><br>Please login to the site for additional user information <a href=\"%s\">%s</a>.\n</body></html>";
	private static final String PASSWORD_RESET = "<html><body>\nYour password has been reset to:<br>%s\n<br><br><br>Please login to the site and create a new password in the <a href=\"%s\">account settings</a>.\n</body></html>";
}
