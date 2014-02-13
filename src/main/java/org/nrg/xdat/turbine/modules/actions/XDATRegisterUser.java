//Copyright Washington University School of Medicine All Rights Reserved
/*
 * Created on Dec 11, 2006
 *
 */
package org.nrg.xdat.turbine.modules.actions;

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
import org.nrg.xdat.services.UserRegistrationDataService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpSession;
import java.util.*;

public class XDATRegisterUser extends VelocitySecureAction {
    static Logger logger = Logger.getLogger(XDATRegisterUser.class);

    @Override
    public void doPerform(RunData data, Context context) throws Exception {
		try {
        	PopulateItem populater = PopulateItem.Populate(data, XFT.PREFIX + ":user", true);
        	ItemI found = populater.getItem();
            ItemSearch search = new ItemSearch();
            search.setAllowMultiples(false);
            search.setElement("xdat:user");
            search.addCriteria("xdat:user.login", found.getProperty("login"));
            
            ItemI temp = search.exec().getFirst();
            
            if (temp == null) {
            	String emailWithWhite = found.getStringProperty("email");
        		String noWhiteEmail = emailWithWhite.trim();
        		found.setProperty("email", noWhiteEmail);
                search = new ItemSearch();
                search.setAllowMultiples(false);
                search.setElement("xdat:user");
                search.addCriteria("xdat:user.email",found.getProperty("email"));
                temp = search.exec().getFirst();

                if (temp == null) {
	                String tempPass = data.getParameters().getString("xdat:user.primary_password"); // the object in found will have run the password through escape character encoding, potentially altering it
	                PasswordValidatorChain validator = XDAT.getContextService().getBean(PasswordValidatorChain.class);
	                if (validator.isValid(tempPass, null)) {
	                
		                // NEW USER
                        String salt = XDATUser.createNewSalt();
	                    found.setProperty("primary_password", new ShaPasswordEncoder(256).encodePassword(tempPass, salt));
	                    found.setProperty("salt", salt);

		                boolean autoApproval = autoApproval(data, context);
		                
		                if (autoApproval) {
		                	if (XDAT.verificationOn() && !hasPAR(data)) {
		                		found.setProperty("enabled", "false");
		                	} else {
		                		found.setProperty("enabled", "true");
		                	}
		                } else {
		                    found.setProperty("enabled", "false");
		                }

                        if (hasPAR(data)) {
                            found.setProperty("verified","true");
                        }
                        else {
                            found.setProperty("verified","false");
                        }
		                
		                XDATUser newUser = new XDATUser(found);

						SaveItemHelper.authorizedSave(newUser, TurbineUtils.getUser(data), true, false, true, false, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Registered User"));
		                
		                XdatUserAuth newUserAuth = new XdatUserAuth((String)found.getProperty("login"), XdatUserAuthService.LOCALDB);
	                    XDAT.getXdatUserAuthService().create(newUserAuth);

                        final String comments = TurbineUtils.HasPassedParameter("comments", data) ? (String) TurbineUtils.GetPassedParameter("comments", data) : "";
                        final String phone = TurbineUtils.HasPassedParameter("phone", data) ? (String) TurbineUtils.GetPassedParameter("phone", data) : "";
                        final String lab = TurbineUtils.HasPassedParameter("lab", data) ? (String) TurbineUtils.GetPassedParameter("lab", data) : "";

                        if (autoApproval) {
                            if (!hasPAR(data) && XDAT.verificationOn()) {
                            	try {
                                    AdminUtils.sendNewUserVerificationEmail(newUser);
            				        context.put("emailTo", newUser.getEmail());
            				        context.put("emailUsername", found.getProperty("login"));
            				        data.setRedirectURI(null);
                                    data.setScreenTemplate("VerificationSent.vm");
                                } catch (Exception e) {
                                    logger.error("Error occurred sending new user email", e);
                                    handleInvalid(data, context, "We are unable to send you the verification email. If you entered a valid email address, please contact our technical support.");
                                }
                            } else {
	                            TurbineUtils.setUser(data, newUser);
	
	                            HttpSession session = data.getSession();
			                    session.setAttribute("user",newUser);
			                    session.setAttribute("loggedin",true);
			                    data.setMessage("User registration complete.");
			                    
			                    session.setAttribute("XNAT_CSRF", UUID.randomUUID().toString());
			                    
			                    if (AdminUtils.GetNewUserRegistrationsEmail()) {
                                    AdminUtils.sendNewUserNotification(newUser, comments, phone, lab, context);
                                }
                                AdminUtils.sendNewUserEmailMessage(newUser.getUsername(), newUser.getEmail(), context);

			                    XFTItem item = XFTItem.NewItem("xdat:user_login",newUser);
			                    Date today = Calendar.getInstance(TimeZone.getDefault()).getTime();
			                    item.setProperty("xdat:user_login.user_xdat_user_id", newUser.getID());
			                    item.setProperty("xdat:user_login.login_date", today);
			                    item.setProperty("xdat:user_login.ip_address", AccessLogger.GetRequestIp(data.getRequest()));
			                    item.setProperty("xdat:user_login.session_id", data.getSession().getId());
                                SaveItemHelper.authorizedSave(item, null, true, false, (EventMetaI) null);

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
		                } else {
		                    try {
		                    	directRequest(data, context, newUser);
		                    } catch(Exception e) {
		                        logger.error(e);
		                    }
		                	
		                    try {
                                cacheRegistrationData(newUser, comments, phone, lab);
                                if (XDAT.verificationOn()) {
                                    // If verification is on, the user must verify their email before the admin gets emailed.
                                    AdminUtils.sendNewUserVerificationEmail(newUser);
            				        context.put("emailTo", newUser.getEmail());
            				        context.put("emailUsername", found.getProperty("login"));
            				        data.setRedirectURI(null);
                                    data.setScreenTemplate("VerificationSent.vm");
                                } else {
                                	AdminUtils.sendNewUserNotification(newUser, comments, phone, lab, context);
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
                    handleInvalid(data, context, "Email (" + found.getProperty("email") + ") already exists.");
                }
            } else {
                //Duplicate Login
                handleInvalid(data, context, "Username (" + found.getProperty("login") + ") already exists.");
            }
        } catch (Exception e) {
            //Other Error
            logger.error("Error Storing User",e);
            handleInvalid(data, context, "Error Storing User.");
        }
    }

    private void cacheRegistrationData(final XDATUser newUser, final String comments, final String phone, final String lab) {
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
