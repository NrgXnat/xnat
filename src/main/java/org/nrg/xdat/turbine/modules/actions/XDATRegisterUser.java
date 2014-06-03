//Copyright Washington University School of Medicine All Rights Reserved
/*
 * Created on Dec 11, 2006
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.servlet.http.HttpSession;

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
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.UserRegistrationDataService;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
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
			UserI found=Users.createUser(TurbineUtils.GetDataParameterHash(data));
			UserI existing=Users.getUser(found.getLogin());
			
            if (existing == null) {
            	String emailWithWhite = found.getEmail();
        		String noWhiteEmail = emailWithWhite.trim();
        		found.setEmail(emailWithWhite);
        		
        		List<UserI> matches=Users.getUsersByEmail(emailWithWhite);

                if (matches.size()==0) {
	                String tempPass = data.getParameters().getString("xdat:user.primary_password"); // the object in found will have run the password through escape character encoding, potentially altering it
	                PasswordValidatorChain validator = XDAT.getContextService().getBean(PasswordValidatorChain.class);
	                if (validator.isValid(tempPass, null)) {
	                
		                // NEW USER
                        String salt = Users.createNewSalt();
                        found.setPassword(new ShaPasswordEncoder(256).encodePassword(tempPass, salt));
                        found.setSalt(salt);

		                boolean autoApproval = autoApproval(data, context);
		                
		                if (autoApproval) {
		                	if (XDAT.verificationOn() && !hasPAR(data)) {
		                		found.setEnabled(Boolean.FALSE);
		                	} else {
		                		found.setEnabled(Boolean.TRUE);
		                	}
		                } else {
	                		found.setEnabled(Boolean.FALSE);
		                }

                        if (hasPAR(data)) {
	                		found.setVerified(Boolean.TRUE);
                        }
                        else {
	                		found.setVerified(Boolean.FALSE);
                        }
		                
                        Users.save(found, TurbineUtils.getUser(data), true, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Registered User"));
						
		                XdatUserAuth newUserAuth = new XdatUserAuth(found.getLogin(), XdatUserAuthService.LOCALDB);
	                    XDAT.getXdatUserAuthService().create(newUserAuth);

                        final String comments = TurbineUtils.HasPassedParameter("comments", data) ? (String) TurbineUtils.GetPassedParameter("comments", data) : "";
                        final String phone = TurbineUtils.HasPassedParameter("phone", data) ? (String) TurbineUtils.GetPassedParameter("phone", data) : "";
                        final String lab = TurbineUtils.HasPassedParameter("lab", data) ? (String) TurbineUtils.GetPassedParameter("lab", data) : "";

                        if (autoApproval) {
                            if (!hasPAR(data) && XDAT.verificationOn()) {
                            	try {
                                    AdminUtils.sendNewUserVerificationEmail(found);
            				        context.put("emailTo", found.getEmail());
            				        context.put("emailUsername", found.getLogin());
            				        data.setRedirectURI(null);
                                    data.setScreenTemplate("VerificationSent.vm");
                                } catch (Exception e) {
                                    logger.error("Error occurred sending new user email", e);
                                    handleInvalid(data, context, "We are unable to send you the verification email. If you entered a valid email address, please contact our technical support.");
                                }
                            } else {
	                            TurbineUtils.setUser(data, found);
	
	                            HttpSession session = data.getSession();
			                    session.setAttribute("user",found);
			                    session.setAttribute("loggedin",true);
			                    data.setMessage("User registration complete.");
			                    
			                    session.setAttribute("XNAT_CSRF", UUID.randomUUID().toString());
			                    
			                    if (AdminUtils.GetNewUserRegistrationsEmail()) {
                                    AdminUtils.sendNewUserNotification(found, comments, phone, lab, context);
                                }
                                AdminUtils.sendNewUserEmailMessage(found.getUsername(), found.getEmail(), context);

			                    XFTItem item = XFTItem.NewItem("xdat:user_login",found);
			                    Date today = Calendar.getInstance(TimeZone.getDefault()).getTime();
			                    item.setProperty("xdat:user_login.user_xdat_user_id", found.getID());
			                    item.setProperty("xdat:user_login.login_date", today);
			                    item.setProperty("xdat:user_login.ip_address", AccessLogger.GetRequestIp(data.getRequest()));
			                    item.setProperty("xdat:user_login.session_id", data.getSession().getId());
                                SaveItemHelper.authorizedSave(item, null, true, false, (EventMetaI) null);

                                Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
			                    grantedAuthorities.add(new GrantedAuthorityImpl("ROLE_USER"));
			    		    	Authentication authentication = new UsernamePasswordAuthenticationToken(found.getLogin(), tempPass, grantedAuthorities);
			    		    	SecurityContext securityContext = SecurityContextHolder.getContext();
			    		    	securityContext.setAuthentication(authentication);
								
			                    try{
			                    	directRequest(data,context,found);
			                    }catch(Exception e){
			                        logger.error(e);
                                    handleInvalid(data, context, "Error directing request after new user was registered.");
			                    }
                            }
		                } else {
		                    try {
		                    	directRequest(data, context, found);
		                    } catch(Exception e) {
		                        logger.error(e);
		                    }
		                	
		                    try {
                                cacheRegistrationData(found, comments, phone, lab);
                                if (XDAT.verificationOn()) {
                                    // If verification is on, the user must verify their email before the admin gets emailed.
                                    AdminUtils.sendNewUserVerificationEmail(found);
            				        context.put("emailTo", found.getEmail());
            				        context.put("emailUsername", found.getLogin());
            				        data.setRedirectURI(null);
                                    data.setScreenTemplate("VerificationSent.vm");
                                } else {
                                	AdminUtils.sendNewUserNotification(found, comments, phone, lab, context);
	                            	data.setRedirectURI(null);
	                                data.setScreenTemplate("PostRegister.vm");
                                }
                            } catch (Exception exception) {
                                //Email send failed
                                logger.error("Error occurred sending new user email", exception);
                                handleInvalid(data, context, "Email send failed. If you are unable to log in to your account, please contact an administrator or create an account with a different email address.");
                            }
		                }
	                } else {
                        //Invalid Password
		            	handleInvalid(data, context, validator.getMessage());
	                }
	            } else {
                    //Duplicate Email
                    handleInvalid(data, context, "Email (" + found.getEmail() + ") already exists.");
                }
            } else {
                //Duplicate Login
                handleInvalid(data, context, "Username (" + found.getLogin() + ") already exists.");
            }
        } catch (Exception e) {
            //Other Error
            logger.error("Error Storing User",e);
            handleInvalid(data, context, "Error Storing User.");
        }
    }

    private void cacheRegistrationData(final UserI newUser, final String comments, final String phone, final String lab) {
        UserRegistrationDataService service = XDAT.getContextService().getBean(UserRegistrationDataService.class);
        service.cacheUserRegistrationData(newUser, phone, lab, comments);
    }

    public boolean hasPAR(RunData data){
        return data.getParameters().containsKey("par") || data.getSession().getAttribute("par") != null;
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
    
    public boolean autoApproval(RunData data, Context context)throws Exception{
    	return XFT.GetUserRegistration();
    }
    
    public void directRequest(RunData data,Context context,UserI user) throws Exception{
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
