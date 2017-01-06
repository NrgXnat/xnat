/*
 * core: org.nrg.xdat.turbine.modules.actions.XDATRegisterUser
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.actions;

import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.modules.actions.VelocitySecureAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.validators.PasswordValidatorChain;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.UserRegistrationDataService;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@SuppressWarnings("unused")
public class XDATRegisterUser extends VelocitySecureAction {
    private static final Logger logger = LoggerFactory.getLogger(XDATRegisterUser.class);

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

		try {
			UserI found=Users.createUser(TurbineUtils.GetDataParameterHash(data));

			if(found.getID()!=null){
                //This shouldn't have a pk yet
                handleInvalid(data, context, "Error registering user account");
                return;
			}

			UserI existing=null;
			try {
				existing = Users.getUser(found.getLogin());
			} catch (Exception ignored) {
			}

            if (existing == null) {
            	String emailWithWhite = found.getEmail();
        		String noWhiteEmail = emailWithWhite.trim();
        		found.setEmail(emailWithWhite);

        		List<? extends UserI> matches=Users.getUsersByEmail(emailWithWhite);
        		List<? extends UserI> matches2=Users.getUsersByEmail(noWhiteEmail);

                if (matches.size()==0 && matches2.size()==0) {
	                String tempPass = data.getParameters().getString("xdat:user.primary_password"); // the object in found will have run the password through escape character encoding, potentially altering it
	                final String message = XDAT.getContextService().getBean(PasswordValidatorChain.class).isValid(tempPass, null);
	                if (StringUtils.isBlank(message)) {
                        // NEW USER
                        found.setPassword(tempPass);

                        final boolean autoApproveRegistered = XDAT.getSiteConfigPreferences().getUserRegistration();
                        final boolean autoApprovePar = XDAT.getSiteConfigPreferences().getPar();
                        final boolean hasParData = hasPAR(data);
                        final boolean enabled = autoApprovePar && hasParData || autoApproveRegistered && (hasParData || !XDAT.getSiteConfigPreferences().getEmailVerification());
                        final boolean verified = !XDAT.getSiteConfigPreferences().getEmailVerification() || hasParData;

                        // Approve them if:
                        //  -- we autoapprove par users and this user has a PAR
                        //  -- we autoapprove registered users and don't require email verification
                        found.setEnabled(enabled);
                        found.setVerified(verified);

                        UserI currUser = XDAT.getUserDetails();
                        UserI userToSave = found;
                        if (currUser != null && !currUser.isGuest()) {
                            userToSave = currUser;
                        }
                        Users.save(found, userToSave, true, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Registered User"));

                        final String comments = TurbineUtils.HasPassedParameter("comments", data) ? (String) TurbineUtils.GetPassedParameter("comments", data) : "";
                        final String phone = TurbineUtils.HasPassedParameter("phone", data) ? (String) TurbineUtils.GetPassedParameter("phone", data) : "";
                        final String lab = TurbineUtils.HasPassedParameter("lab", data) ? (String) TurbineUtils.GetPassedParameter("lab", data) : "";

                        if (enabled) {
                            if (!verified) {
                                try {
                                    sendNewUserVerificationEmail(data, context, found);
                                } catch (Exception e) {
                                    logger.error("Error occurred sending new user email", e);
                                    handleInvalid(data, context, "We are unable to send you the verification email. If you entered a valid email address, please contact our technical support.");
                                }
                            } else {
                                Authentication authentication = XDAT.setUserDetails(found);
                                UserHelper.setUserHelper(data.getRequest(), found);

                                data.setMessage("User registration complete.");

                                AdminUtils.sendNewUserNotification(found, comments, phone, lab, context);
                                AdminUtils.sendNewUserEmailMessage(found.getUsername(), found.getEmail());

                                XFTItem item = XFTItem.NewItem("xdat:user_login", found);
                                Date today = Calendar.getInstance(TimeZone.getDefault()).getTime();
                                item.setProperty("xdat:user_login.user_xdat_user_id", found.getID());
                                item.setProperty("xdat:user_login.login_date", today);
                                item.setProperty("xdat:user_login.ip_address", AccessLogger.GetRequestIp(data.getRequest()));
                                item.setProperty("xdat:user_login.session_id", data.getSession().getId());
                                SaveItemHelper.authorizedSave(item, null, true, false, (EventMetaI) null);

                                if (!found.isGuest()) {
                                    SecurityContextHolder.getContext().setAuthentication(authentication);
                                }

                                try {
                                    directRequest(data, context, found);
                                } catch (Exception e) {
                                    logger.error("Error directing request after new user was registered.", e);
                                    handleInvalid(data, context, "Error directing request after new user was registered.");
                                }
                            }
                        } else {
                            try {
                                directRequest(data, context, found);
                            } catch (Exception e) {
                                logger.error("", e);
                            }

                            try {
                                cacheRegistrationData(found, comments, phone, lab);
                                if (!found.isVerified()) {
                                    sendNewUserVerificationEmail(data, context, found);
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
		            	handleInvalid(data, context, message);
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

    protected void sendNewUserVerificationEmail(final RunData data, final Context context, final UserI found) throws Exception {
        // If verification is on, the user must verify their email before the admin gets emailed.
        AdminUtils.sendNewUserVerificationEmail(found);
        context.put("emailTo", found.getEmail());
        context.put("emailUsername", found.getLogin());
        data.setRedirectURI(null);
        data.setScreenTemplate("VerificationSent.vm");
    }

    private void cacheRegistrationData(final UserI newUser, final String comments, final String phone, final String lab) throws NrgServiceException {
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

    public void directRequest(RunData data,Context context,UserI user) throws Exception{
		String nextPage = (String)TurbineUtils.GetPassedParameter("nextPage",data);
		String nextAction = (String)TurbineUtils.GetPassedParameter("nextAction",data);

        data.setScreenTemplate("Index.vm");

        if ((XDAT.getSiteConfigPreferences().getUserRegistration() && !XDAT.getSiteConfigPreferences().getEmailVerification()) || ((XDAT.getSiteConfigPreferences().getUserRegistration() || XDAT.getSiteConfigPreferences().getPar()) && hasPAR(data))){
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
