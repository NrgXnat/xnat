/*
 * core: org.nrg.xdat.turbine.modules.actions.XDATRegisterUser
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.actions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.modules.actions.VelocitySecureAction;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.parser.ParameterParser;
import org.apache.velocity.context.Context;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.validators.PasswordValidatorChain;
import org.nrg.xdat.services.UserRegistrationDataService;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;

import java.util.List;

@SuppressWarnings("unused")
@Slf4j
public class XDATRegisterUser extends VelocitySecureAction {
    public XDATRegisterUser() {
        this("Register.vm");
    }

    protected XDATRegisterUser(final String pageForRetry) {
        _pageForRetry = pageForRetry;
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

		try {
            final UserI found = Users.createUser(TurbineUtils.GetDataParameterHash(data));

            if (found.getID() != null) {
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
            	final String emailWithWhite = found.getEmail();
            	if (StringUtils.isBlank(emailWithWhite)) {
                    handleInvalid(data, context, "No valid email address provided.");
                    return;
                }

        		String noWhiteEmail = emailWithWhite.trim();
        		found.setEmail(emailWithWhite);

        		List<? extends UserI> matches=Users.getUsersByEmail(emailWithWhite);
        		List<? extends UserI> matches2=Users.getUsersByEmail(noWhiteEmail);

                if (matches.size()==0 && matches2.size()==0) {
                    final ParameterParser parameters = data.getParameters();
                    final String          operation  = parameters.getString("operation");
	                final String          tempPass   = parameters.getString("xdat:user.primary_password"); // the object in found will have run the password through escape character encoding, potentially altering it

                    // If this is a register operation, we don't want to validate the password because there isn't one: we're just
                    // creating the XNAT account to correspond with the auth account.
	                final String message;
	                if (StringUtils.equals("register", operation)) {
	                    message = "";
                    } else {
                        message = XDAT.getContextService().getBean(PasswordValidatorChain.class).isValid(tempPass, null);
                    }

                    if (StringUtils.isBlank(message)) {
                        // NEW USER
                        found.setPassword(tempPass);

                        final SiteConfigPreferences preferences  = XDAT.getSiteConfigPreferences();

                        final boolean hasParData             = hasPAR(data);
                        final boolean autoApprovePar         = preferences.getPar();
                        final boolean autoEnable             = preferences.getUserRegistration();
                        final boolean autoVerify             = !preferences.getEmailVerification();
                        final String  authMethod             = parameters.getString("authMethod");
                        final String  providerId             = parameters.getString("providerId");
                        final boolean isProviderAutoEnabled  = parameters.getBoolean("providerAutoEnabled", false);
                        final boolean isProviderAutoVerified = parameters.getBoolean("providerAutoVerified", false);

                        // Approve them if:
                        //  -- we autoapprove registered users
                        //  -- authenticating provider autoapproves users
                        //  -- we autoapprove par users and this user has a PAR
                        // Verify them if:
                        //  -- we autoverify users (don't require email verification)
                        //  -- authenticating provider autoverifies users
                        //  -- this user has a PAR (includes email so address is already verified)
                        final boolean enabled  = autoEnable || isProviderAutoEnabled || autoApprovePar && hasParData;
                        final boolean verified = autoVerify || isProviderAutoVerified || hasParData;

                        found.setEnabled(enabled);
                        found.setVerified(verified);

                        final UserI currUser = XDAT.getUserDetails();
                        final UserI userToSave = currUser != null && !currUser.isGuest() ? currUser : found;

                        final XdatUserAuth userAuth = (XdatUserAuth) context.get("userAuth");
                        if (userAuth != null) {
                            Users.save(found, userToSave, userAuth, true, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Registered User"));
                        } else {
                            Users.save(found, userToSave, true, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Registered User"));
                        }

                        final String comments = TurbineUtils.HasPassedParameter("comments", data) ? (String) TurbineUtils.GetPassedParameter("comments", data) : "";
                        final String phone = TurbineUtils.HasPassedParameter("phone", data) ? (String) TurbineUtils.GetPassedParameter("phone", data) : "";
                        final String lab = TurbineUtils.HasPassedParameter("lab", data) ? (String) TurbineUtils.GetPassedParameter("lab", data) : "";

                        if (enabled) {
                            if (!verified) {
                                try {
                                    sendNewUserVerificationEmail(data, context, found);
                                } catch (Exception e) {
                                    log.error("Error occurred sending new user email", e);
                                    handleInvalid(data, context, "We are unable to send you the verification email. If you entered a valid email address, please contact our technical support.");
                                }
                            } else {
                                XDAT.loginUser(found, data.getRequest(), tempPass);
                                data.setMessage("User registration complete.");

                                AdminUtils.sendNewUserNotification(found, comments, phone, lab, context);
                                AdminUtils.sendNewUserEmailMessage(found.getUsername(), found.getEmail());

                                try {
                                    directRequest(data, context, found);
                                } catch (Exception e) {
                                    log.error("Error directing request after new user was registered.", e);
                                    handleInvalid(data, context, "Error directing request after new user was registered.");
                                }
                            }
                        } else {
                            try {
                                directRequest(data, context, found);
                            } catch (Exception e) {
                                log.error("An error occurred trying to run directRequest()", e);
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
                                log.error("Error occurred sending new user email", exception);
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
            log.error("Error Storing User",e);
            handleInvalid(data, context, "Error Storing User.");
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
            log.error(message,e);
            data.setMessage(message);
        }finally{
            data.setScreenTemplate(getPageForRetry());
        }
    }

    @Override
    protected boolean isAuthorized(RunData data) {
        return true;
    }

    protected String getPageForRetry() {
        return _pageForRetry;
    }

    protected void sendNewUserVerificationEmail(final RunData data, final Context context, final UserI found) throws Exception {
        // If verification is on, the user must verify their email before the admin gets emailed.
        AdminUtils.sendNewUserVerificationEmail(found);
        context.put("emailTo", found.getEmail());
        context.put("emailUsername", found.getLogin());
        context.put("siteLogoPath", XDAT.getSiteLogoPath());
        data.setRedirectURI(null);
        data.setScreenTemplate("VerificationSent.vm");
    }

    protected void preserveVariables(final RunData data, final Context context) {
        storeParameterIfAvailable(data, context, "username", "xdat:user.login");
        storeParameterIfAvailable(data, context, "email", "xdat:user.email");
        storeParameterIfAvailable(data, context, "firstName", "xdat:user.firstname");
        storeParameterIfAvailable(data, context, "lastName", "xdat:user.lastname");
        storeParameterIfAvailable(data, context, "par", "par");
    }

    protected void storeParameterIfAvailable(final RunData data, final Context context, final String key, final String value) {
        if (TurbineUtils.HasPassedParameter(value, data)) {
            context.put(key, data.getParameters().getString(value));
        }
    }

    private void cacheRegistrationData(final UserI newUser, final String comments, final String phone, final String lab) throws NrgServiceException {
        UserRegistrationDataService service = XDAT.getContextService().getBean(UserRegistrationDataService.class);
        service.cacheUserRegistrationData(newUser, phone, lab, comments);
    }

    private final String _pageForRetry;
}
