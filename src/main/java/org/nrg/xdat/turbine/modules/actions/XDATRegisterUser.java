//Copyright Washington University School of Medicine All Rights Reserved
/*
 * Created on Dec 11, 2006
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.services.AliasTokenService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.modules.actions.VelocitySecureAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.security.PasswordValidatorChain;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.utils.SaveItemHelper;
import org.springframework.mail.MailSendException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class XDATRegisterUser extends VelocitySecureAction {
    static Logger logger = Logger.getLogger(XDATRegisterUser.class);

    @Override
    public void doPerform(RunData data, Context context) throws Exception {
		try {
        	PopulateItem populater = PopulateItem.Populate(data,org.nrg.xft.XFT.PREFIX + ":user",true);
        	ItemI found = populater.getItem();
            ItemSearch search = new ItemSearch();
            search.setAllowMultiples(false);
            search.setElement("xdat:user");
            search.addCriteria("xdat:user.login",found.getProperty("login"));
            
            ItemI temp = search.exec().getFirst();
            
            if (temp==null)
            {
            	String emailWithWhite = found.getStringProperty("email");
        		String noWhiteEmail = emailWithWhite.trim();
        		found.setProperty("email", noWhiteEmail);
                search = new ItemSearch();
                search.setAllowMultiples(false);
                search.setElement("xdat:user");
                search.addCriteria("xdat:user.email",found.getProperty("email"));
                temp = search.exec().getFirst();

                if (temp==null)
                {
	                String tempPass = found.getStringProperty("primary_password");
	                PasswordValidatorChain validator = XDAT.getContextService().getBean(PasswordValidatorChain.class);
	                if(validator.isValid(tempPass, null)){
	                
		             // NEW USER
	                    found.setProperty("primary_password",XDATUser.EncryptString(tempPass,"SHA-256"));
	
		                boolean autoApproval=autoApproval(data,context);	       
		                
		                if (autoApproval)
		                {
		                	if(XDAT.verificationOn()){
		                		found.setProperty("enabled","false");
		                	}
		                	else{
		                		found.setProperty("enabled","true");
		                	}
		                    
		                }else{
		                    found.setProperty("enabled","false");
		                }
		                
		                found.setProperty("verified","false");
		                
		                found.setProperty("xdat:user.assigned_roles.assigned_role[0].role_name","SiteUser");
	                    found.setProperty("xdat:user.assigned_roles.assigned_role[1].role_name","DataManager");
		                
		                XDATUser newUser = new XDATUser(found);

						
						SaveItemHelper.authorizedSave(newUser, TurbineUtils.getUser(data),true,false,true,false,EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Registered User"));
		                
		                XdatUserAuth newUserAuth = new XdatUserAuth((String)found.getProperty("login"), XdatUserAuthService.LOCALDB);
	                    XDAT.getXdatUserAuthService().create(newUserAuth);
	
		                if (autoApproval)
		                {
                            if(XDAT.verificationOn()){
                            	try {
                            		String subject = TurbineUtils.GetSystemName() + " Email Verification";
                            		String admin = AdminUtils.getAdminEmailId();
                                	String to = newUser.getEmail();
            				        AliasToken token = XDAT.getContextService().getBean(AliasTokenService.class).issueTokenForUser(newUser,true,null);
            				        String text = "Dear " + newUser.getFirstname() + " " + newUser.getLastname() + ",<br/>\r\n" + "Please click this link to verify your email address: " + TurbineUtils.GetFullServerPath() + "/app/template/VerifyEmail.vm?a=" + token.getAlias() + "&s=" + token.getSecret() + "<br/>\r\nThis link will expire in 24 hours.";
                                    XDAT.getMailService().sendHtmlMessage(admin, to, subject, text);
            				        context.put("emailTo", to);
            				        context.put("emailUsername", (String)found.getProperty("login"));
            				        data.setRedirectURI(null);
                                    data.setScreenTemplate("VerificationSent.vm");
                                } catch (Exception e) {
                                    logger.error("Error occurred sending new user email", e);
                                    handleInvalid(data, context, "We are unable to send you the verification email. If you entered a valid email address, please contact our technical support.");
                                    return;
                                }
                            }
                            else{
	                            TurbineUtils.setUser(data, newUser);
	
	                            HttpSession session = data.getSession();
			                    session.setAttribute("user",newUser);
			                    session.setAttribute("loggedin",true);
			                    data.setMessage("User registration complete.");
			                    
			                    session.setAttribute("XNAT_CSRF", UUID.randomUUID().toString());
			                    
			                    String sub = "New User Created: " + newUser.getUsername();
			                    String msg = this.getAutoApprovalTextMsg(data,newUser);
			                    
			                    
			                    if (AdminUtils.GetNewUserRegistrationsEmail())
			                        AdminUtils.sendAdminEmail(sub, msg);
			
			                    XFTItem item = XFTItem.NewItem("xdat:user_login",newUser);
			                    java.util.Date today = java.util.Calendar.getInstance(java.util.TimeZone.getDefault()).getTime();
			                    item.setProperty("xdat:user_login.user_xdat_user_id",newUser.getID());
			                    item.setProperty("xdat:user_login.login_date",today);
			                    item.setProperty("xdat:user_login.ip_address",AccessLogger.GetRequestIp(data.getRequest()));
			                    SaveItemHelper.authorizedSave(item,null,true,false,(EventMetaI)null);
			                    
								Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
			                    grantedAuthorities.add(new GrantedAuthorityImpl("ROLE_USER"));
			    		    	Authentication authentication = new UsernamePasswordAuthenticationToken(found.getProperty("login"), tempPass, grantedAuthorities);
			    		    	SecurityContext securityContext = SecurityContextHolder.getContext();
			    		    	securityContext.setAuthentication(authentication);
								
			                    try{
			                    	directRequest(data,context,newUser);
			                    }catch(Exception e){
			                        logger.error(e);
                                    handleInvalid(data, context, "Error directing request after new user was registered.");
			                    }
                            }
		                }
                        else{
		                    
		                    try{
		                    	directRequest(data,context,newUser);
		                    }catch(Exception e){
		                        logger.error(e);
		                    }
		                	
		                    String comments = "";
		                    if (TurbineUtils.HasPassedParameter("comments", data))
		                        comments = (String)TurbineUtils.GetPassedParameter("comments", data);
		                              
		                    String phone = "";
		                    if (TurbineUtils.HasPassedParameter("phone", data))
		                        phone = (String)TurbineUtils.GetPassedParameter("phone", data);
		                              
		                    String lab = "";
		                    if (TurbineUtils.HasPassedParameter("lab", data))
		                        lab = (String)TurbineUtils.GetPassedParameter("lab", data);

		                    try {
                                if(XDAT.verificationOn()){
                                	// If verification is on, the user must verify their email before the admin gets emailed.
                            		String subject = TurbineUtils.GetSystemName() + " Email Verification";
                            		String admin = AdminUtils.getAdminEmailId();
                                	String to = newUser.getEmail();
            				        AliasToken token = XDAT.getContextService().getBean(AliasTokenService.class).issueTokenForUser(newUser,true,null);
            				        String text = "Dear " + newUser.getFirstname() + " " + newUser.getLastname() + ",<br/>\r\n" + "Please click this link to verify your email address: " + TurbineUtils.GetFullServerPath() + "/app/template/VerifyEmail.vm?a=" + token.getAlias() + "&s=" + token.getSecret() + "<br/>\r\nThis link will expire in 24 hours.";
            				        XDAT.getMailService().sendHtmlMessage(admin, to, subject, text);
            				        context.put("emailTo", to);
            				        context.put("emailUsername", (String)found.getProperty("login"));
            				        data.setRedirectURI(null);
                                    data.setScreenTemplate("VerificationSent.vm");
                                }
                                else{
                                	AdminUtils.sendNewUserRequestNotification(newUser.getUsername(), newUser.getFirstname(), newUser.getLastname(), newUser.getEmail(), comments, phone, lab, context);
	                            	data.setRedirectURI(null);
	                                data.setScreenTemplate("PostRegister.vm");
                                }
                            } catch (Exception exception) {
                                //Email send failed
                                logger.error("Error occurred sending new user email", exception);
                                handleInvalid(data, context, "Email send failed. If you are unable to log in to your account, please contact an administrator or create an account with a different email address.");
                            }
		                }
	                }else{
                        //Invalid Password
		            	handleInvalid(data, context, validator.getMessage());
	                }
	            }else{
                    //Duplicate Email
                    handleInvalid(data, context, "Email (" + found.getProperty("email") + ") already exists.");
                }
            }else{
                //Duplicate Login
                handleInvalid(data, context, "Username (" + found.getProperty("login") + ") already exists.");
            }
        } catch (Exception e) {
            //Other Error
            logger.error("Error Storing User",e);
            handleInvalid(data, context, "Error Storing User.");
        }
    }

    public void handleInvalid(RunData data, Context context, String message)  {
        try {
            String nextPage = (String)TurbineUtils.GetPassedParameter("nextPage",data);
            String nextAction = (String)TurbineUtils.GetPassedParameter("nextAction",data);

            preserveVariables(data, context);

            if (!StringUtils.isEmpty(nextAction) && !nextAction.contains("XDATLoginUser") && !nextAction.equals(org.apache.turbine.Turbine.getConfiguration().getString("action.login"))){
                context.put("nextAction", nextAction);
            }else if (!StringUtils.isEmpty(nextPage) && !nextPage.equals(org.apache.turbine.Turbine.getConfiguration().getString("template.home")) ) {
                context.put("nextPage", nextPage);
            }
            // OLD USER
            data.setMessage(message);
        } catch (Exception e) {
            logger.error(message,e);
            data.setMessage(message);
        }finally{
            data.setScreenTemplate("Register.vm");
        }
    }

    private void preserveVariables(RunData data,Context context){
        String username = TurbineUtils.HasPassedParameter("xdat:user.login", data)?((String)TurbineUtils.GetPassedParameter("xdat:user.login", data)):"";
        String email = TurbineUtils.HasPassedParameter("xdat:user.email", data)?((String)TurbineUtils.GetPassedParameter("xdat:user.email", data)):"";
        String firstName = TurbineUtils.HasPassedParameter("xdat:user.firstname", data)?((String)TurbineUtils.GetPassedParameter("xdat:user.firstname", data)):"";
        String lastName = TurbineUtils.HasPassedParameter("xdat:user.lastname", data)?((String)TurbineUtils.GetPassedParameter("xdat:user.lastname", data)):"";
        String par = (String)TurbineUtils.GetPassedParameter("par",data);
        //phone, lab, and comments should already be preserved
        if(!StringUtils.isEmpty(username)){
            context.put("username", username);
        }
        if(!StringUtils.isEmpty(email)){
            context.put("email", email);
        }
        if(!StringUtils.isEmpty(firstName)){
            context.put("firstName", firstName);
        }
        if(!StringUtils.isEmpty(lastName)){
            context.put("lastName", lastName);
        }
        if(!StringUtils.isEmpty(par)){
            context.put("par", par);
        }
    }
    
    public String getAutoApprovalTextMsg(RunData data, XDATUser newUser){
    	String msg="New User Created: " + newUser.getUsername();
        msg +="<br>Firstname: " + newUser.getFirstname();
        msg +="<br>Lastname: " + newUser.getLastname();
        msg +="<br>Email: " + newUser.getEmail();
        if (TurbineUtils.HasPassedParameter("comments", data))
            msg +="<br>Comments: " + TurbineUtils.GetPassedParameter("comments", data);
        
        if (TurbineUtils.HasPassedParameter("phone", data))
            msg +="<br>Phone: " + TurbineUtils.GetPassedParameter("phone", data);
        
        if (TurbineUtils.HasPassedParameter("lab", data))
            msg +="<br>Lab: " + TurbineUtils.GetPassedParameter("lab", data);
        
        return msg;
    }
    
    public boolean autoApproval(RunData data, Context context)throws Exception{
    	return XFT.GetUserRegistration();
    }
    
    public void directRequest(RunData data,Context context,XDATUser user) throws Exception{
		String nextPage = (String)TurbineUtils.GetPassedParameter("nextPage",data);
		String nextAction = (String)TurbineUtils.GetPassedParameter("nextAction",data);

        data.setScreenTemplate("Index.vm");
        
        if (XFT.GetUserRegistration() && !XDAT.verificationOn()){
         if (!StringUtils.isEmpty(nextAction) && !nextAction.contains("XDATLoginUser") && !nextAction.equals(org.apache.turbine.Turbine.getConfiguration().getString("action.login"))){
			data.setAction(nextAction);
            VelocityAction action = (VelocityAction) ActionLoader.getInstance().getInstance(nextAction);
            action.doPerform(data, context);
		 }else if (!StringUtils.isEmpty(nextPage) && !nextPage.equals(org.apache.turbine.Turbine.getConfiguration().getString("template.home")) ) {
			data.setScreenTemplate(nextPage);
		 }
        }
    }

    @Override
    protected boolean isAuthorized(RunData data) throws Exception {
        return true;
    }
}
