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
import org.apache.turbine.Turbine;
import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.modules.actions.VelocitySecureAction;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.parser.ParameterParser;
import org.apache.velocity.context.Context;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.utilities.Patterns;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

@SuppressWarnings({"unused", "DuplicatedCode"})
@Slf4j
public class XDATRegisterUser extends VelocitySecureAction {
    public static final String XDAT_USER_FIRSTNAME = "xdat:user.firstname";
    public static final String XDAT_USER_LASTNAME  = "xdat:user.lastname";
    public static final String XDAT_USER_LOGIN     = "xdat:user.login";
    public static final String XDAT_USER_EMAIL     = "xdat:user.email";
    public static final String XDAT_USER_PAR       = "par";

    public XDATRegisterUser() {
        this("Register.vm");
    }

    protected XDATRegisterUser(final String pageForRetry) {
        _preferences = XDAT.getSiteConfigPreferences();
        _service = XDAT.getContextService().getBean(UserRegistrationDataService.class);
        _pageForRetry = pageForRetry;
        _hasValidated = new AtomicBoolean();
    }

    @Override
    public void doPerform(final RunData data, final Context context) throws Exception {
        //noinspection Duplicates
        try {
            SecureAction.isCsrfTokenOk(data);
        } catch (Exception e1) {
            data.setMessage("Due to a technical issue, the requested action cannot be performed.");
            data.setScreenTemplate("Login.vm");
            return;
        }

        if (!validate(data, context)) {
            return;
        }

        try {
            final UserI found = Users.createUser(TurbineUtils.GetDataParameterHash(data));

            if (found.getID() != null) {
                //This shouldn't have a pk yet
                handleInvalid(data, context, "Error registering user account");
                return;
            }

            UserI existing = null;
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

                List<? extends UserI> matches  = Users.getUsersByEmail(emailWithWhite);
                List<? extends UserI> matches2 = Users.getUsersByEmail(noWhiteEmail);

                if (matches.size() == 0 && matches2.size() == 0) {
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

                        final boolean hasParData             = hasPAR(data);
                        final boolean autoApprovePar         = _preferences.getPar();
                        final boolean autoEnable             = _preferences.getUserRegistration();
                        final boolean autoVerify             = !_preferences.getEmailVerification();
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

                        final UserI currUser   = XDAT.getUserDetails();
                        final UserI userToSave = currUser != null && !currUser.isGuest() ? currUser : found;

                        final XdatUserAuth userAuth = (XdatUserAuth) context.get("userAuth");
                        if (userAuth != null) {
                            Users.save(found, userToSave, userAuth, true, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Registered User"));
                        } else {
                            Users.save(found, userToSave, true, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Registered User"));
                        }

                        final String comments = TurbineUtils.HasPassedParameter("comments", data) ? (String) TurbineUtils.GetPassedParameter("comments", data) : "";
                        final String phone    = TurbineUtils.HasPassedParameter("phone", data) ? (String) TurbineUtils.GetPassedParameter("phone", data) : "";
                        final String lab      = TurbineUtils.HasPassedParameter("lab", data) ? (String) TurbineUtils.GetPassedParameter("lab", data) : "";

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
            log.error("Error Storing User", e);
            handleInvalid(data, context, "Error Storing User.");
        }
    }

    public void directRequest(final RunData data, final Context context, final UserI user) throws Exception {
        String nextPage   = (String) TurbineUtils.GetPassedParameter("nextPage", data);
        String nextAction = (String) TurbineUtils.GetPassedParameter("nextAction", data);

        data.setScreenTemplate("Index.vm");

        if ((getPreferences().getUserRegistration() && !getPreferences().getEmailVerification()) || ((getPreferences().getUserRegistration() || getPreferences().getPar()) && hasPAR(data))) {
            if (!StringUtils.isEmpty(nextAction) && !nextAction.contains("XDATLoginUser") && !nextAction.equals(Turbine.getConfiguration().getString("action.login"))) {
                data.setAction(nextAction);
                final VelocityAction action = (VelocityAction) ActionLoader.getInstance().getInstance(nextAction);
                action.doPerform(data, context);
            } else if (!StringUtils.isEmpty(nextPage) && !nextPage.equals(Turbine.getConfiguration().getString("template.home"))) {
                data.setScreenTemplate(nextPage);
            }
        }
    }

    public boolean hasPAR(final RunData data) {
        return data.getParameters().containsKey(XDAT_USER_PAR) || data.getSession().getAttribute(XDAT_USER_PAR) != null;
    }

    public void handleInvalid(final RunData data, final Context context, final String message) {
        try {
            final String nextPage   = (String) TurbineUtils.GetPassedParameter("nextPage", data);
            final String nextAction = (String) TurbineUtils.GetPassedParameter("nextAction", data);

            preserveVariables(data, context);

            if (!StringUtils.isEmpty(nextAction) && !nextAction.contains("XDATLoginUser") && !nextAction.equals(Turbine.getConfiguration().getString("action.login"))) {
                context.put("nextAction", nextAction);
            } else if (!StringUtils.isEmpty(nextPage) && !nextPage.equals(Turbine.getConfiguration().getString("template.home"))) {
                context.put("nextPage", nextPage);
            }
            // OLD USER
            data.setMessage(message);
        } catch (Exception e) {
            log.error(message, e);
            data.setMessage(message);
        } finally {
            data.setScreenTemplate(getPageForRetry());
        }
    }

    @Override
    protected boolean isAuthorized(final RunData data) {
        return true;
    }

    protected SiteConfigPreferences getPreferences() {
        return _preferences;
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
        storeParameterIfAvailable(data, context, "username", XDAT_USER_LOGIN);
        storeParameterIfAvailable(data, context, "email", XDAT_USER_EMAIL);
        storeParameterIfAvailable(data, context, "firstName", XDAT_USER_FIRSTNAME);
        storeParameterIfAvailable(data, context, "lastName", XDAT_USER_LASTNAME);
        storeParameterIfAvailable(data, context, XDAT_USER_PAR, XDAT_USER_PAR);
    }

    protected void storeParameterIfAvailable(final RunData data, final Context context, final String key, final String value) {
        if (TurbineUtils.HasPassedParameter(value, data)) {
            context.put(key, data.getParameters().getString(value));
        }
    }

    protected boolean validate(final RunData data, final Context context) {
        if (_hasValidated.get()) {
            return true;
        }

        try {
            // Tests for valid values
            final List<String> failures = new ArrayList<>();
            if (isInvalidParameter(data, XDAT_USER_EMAIL, Patterns.EMAIL)) {
                failures.add("Email");
            }
            if (isInvalidParameter(data, XDAT_USER_LOGIN, Patterns.USERNAME)) {
                failures.add("Username");
            }
            if (isInvalidParameter(data, XDAT_USER_FIRSTNAME, NAME)) {
                failures.add("First name");
            }
            if (isInvalidParameter(data, XDAT_USER_LASTNAME, NAME)) {
                failures.add("Last name");
            }
            if (isInvalidParameter(data, XDAT_USER_PAR, PAR, false)) {
                failures.add("Project access request");
            }
            if (!failures.isEmpty()) {
                handleInvalid(data, context, "Please verify and re-enter the value(s) for the following fields: " + String.join(", ", failures));
                return false;
            }
            return true;
        } finally {
            _hasValidated.set(true);
        }
    }

    private void cacheRegistrationData(final UserI newUser, final String comments, final String phone, final String lab) throws NrgServiceException {
        _service.cacheUserRegistrationData(newUser, phone, lab, comments);
    }

    private static boolean isInvalidParameter(final RunData data, final String name, final Pattern pattern) {
        return isInvalidParameter(data, name, pattern, true);
    }

    private static boolean isInvalidParameter(final RunData data, final String name, final Pattern pattern, final boolean required) {
        if (!data.getParameters().containsKey(name)) {
            return required;
        }
        return !pattern.matcher(data.getParameters().getString(name)).matches();
    }

    private static final Pattern NAME = Pattern.compile("^([a-z0-9 '\".,_-]+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAR  = Pattern.compile("^([0-9]+|[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?)$", Pattern.CASE_INSENSITIVE);

    private final SiteConfigPreferences       _preferences;
    private final UserRegistrationDataService _service;
    private final String                      _pageForRetry;
    private final AtomicBoolean               _hasValidated;
}
