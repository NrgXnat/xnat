/*
 * core: org.nrg.xdat.turbine.modules.actions.XDATForgotLogin
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.actions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.modules.actions.VelocitySecureAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.mail.services.EmailRequestLogService;
import org.nrg.mail.services.MailService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.mail.MessagingException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class XDATForgotLogin extends VelocitySecureAction {
    public XDATForgotLogin() {
        _requestLog = XDAT.getContextService().getBean(EmailRequestLogService.class);
        _userAuthService = XDAT.getContextService().getBean(XdatUserAuthService.class);
        _aliasTokenService = XDAT.getContextService().getBean(AliasTokenService.class);
        _mailService = XDAT.getMailService();
        _notifications = XDAT.getNotificationsPreferences();
        _siteConfig = XDAT.getSiteConfigPreferences();
    }

    public void additionalProcessing(final RunData data, final Context context, final UserI user) throws Exception {
        log.debug("Now in default additional processing block of XDATForgotLogin for user {}", user.getUsername());
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

        final String email    = ((String) TurbineUtils.GetPassedParameter("email", data));
        final String username = ((String) TurbineUtils.GetPassedParameter("username", data));
        final String subject  = TurbineUtils.GetSystemName() + " Login Request";
        final String admin    = _siteConfig.getAdminEmail();

        if (StringUtils.isAllBlank(email, username)) {
            data.setScreenTemplate("ForgotLogin.vm");
            return;
        }

        if (!StringUtils.isBlank(email)) {
            //check email
            final List<? extends UserI> users = Users.getUsersByEmail(email);

            if (users == null || users.size() == 0) {
                data.setMessage("Unknown email address.");
                data.setScreenTemplate("ForgotLogin.vm");
                return;
            }

            final UserI user = users.get(0);
            try {
                additionalProcessing(data, context, user);
            } catch (Exception e) {
                log.error("An error occurred while doing additional processing on password reset request by user {}", user.getUsername(), e);
            }

            try {
                if (_requestLog != null && _requestLog.isEmailBlocked(email)) {
                    data.setMessage("You have exceeded the allowed number of email requests. Please try again later.");
                    data.setScreenTemplate("Login.vm");
                } else {
                    final List<XdatUserAuth> auths = _userAuthService.getUsersByXdatUsername(user.getUsername());
                    final String             usernames;
                    if (auths.isEmpty()) {
                        usernames = user.getUsername();
                    } else if (auths.size() == 1) {
                        usernames = auths.get(0).getAuthUser();
                    } else {
                        usernames = String.format("<br><br><ul><li>%s</li></ul>", auths.stream().map(XdatUserAuth::getAuthUser).collect(Collectors.joining("</li><li>")));
                    }
                    String body = _notifications.getEmailMessageForgotUsernameRequest();
                    body = XDAT.getNotificationsPreferences().replaceCommonAnchorTags(body, user);
                    body = XDAT.getNotificationsPreferences().replaceBackwardsCompatibleEmailInconsistencies(body);
                    _mailService.sendHtmlMessage(admin, email, subject, body);
                    if (_requestLog != null) {
                        _requestLog.logEmailRequest(email, new Date());
                    }
                    data.setMessage("The corresponding username for this email address has been emailed to your account.");
                    data.setScreenTemplate("Login.vm");
                }
            } catch (MessagingException exception) {
                log.error("An error occurred trying to send a username retrieval request for user {}", user.getUsername(), exception);
                data.setMessage("Due to a technical difficulty, we are unable to send you the email containing your information.  Please contact our technical support.");
                data.setScreenTemplate("ForgotLogin.vm");
            }
            return;
        }

        if (userAccountIsManagedExternally(username, data)) {
            return;
        }

        //check user
        final UserI user;
        try {
            user = Users.getUser(_userAuthService.getUsersByName(username).stream().filter(auth -> auth.getAuthMethod().equals(XdatUserAuthService.LOCALDB)).findFirst().orElseThrow(() -> new UsernameNotFoundException(username)).getXdatUsername());
        } catch (UsernameNotFoundException e) {
            data.setMessage("Unknown username \"" + e.getMessage() + "\"");
            data.setScreenTemplate("ForgotLogin.vm");
            return;
        }

        try {
            additionalProcessing(data, context, user);
        } catch (Exception e) {
            log.error("An error occurred while doing additional processing on password reset request by user {}", username, e);
        }

        // If the user is enabled, go ahead and do this stuff.
        if (user.isEnabled()) {
            try {
                final String to = user.getEmail();
                if (_requestLog != null && _requestLog.isEmailBlocked(to)) {
                    data.setMessage("You have exceeded the allowed number of email requests. Please try again later.");
                    data.setScreenTemplate("Login.vm");
                } else {
                    final AliasToken token   = _aliasTokenService.issueTokenForUser(user, false, null, TWO_HOURS_IN_SECONDS);
                    String body = XDAT.getNotificationsPreferences().getEmailMessageForgotPasswordReset();
                    body = XDAT.getNotificationsPreferences().replaceCommonAnchorTags(body, user);

                    String resetUrl = TurbineUtils.GetFullServerPath() + "/app/template/XDATScreen_UpdateUser.vm?a=" + token.getAlias() + "&s=" + token.getSecret();

                    String resetLink = "<a href=\"" + resetUrl + "\">" + "Reset Password" + "</a>";
                    body=body.replaceAll("RESET_URL",resetUrl);
                    body = body.replaceAll("RESET_LINK", resetLink);
                    body = XDAT.getNotificationsPreferences().replaceBackwardsCompatibleEmailInconsistencies(body);
                    _mailService.sendHtmlMessage(admin, to, subject, body);
                    if (_requestLog != null) {
                        _requestLog.logEmailRequest(to, new Date());
                    }
                    data.setMessage("You have been sent an email with a link to reset your password. Please check your email.");
                    data.setScreenTemplate("Login.vm");
                }
            } catch (MessagingException e) {
                log.error("Unable to send mail", e);
                data.setMessage("Due to a technical difficulty, we are unable to send you the email containing your information.  Please contact our technical support.");
                data.setScreenTemplate("ForgotLogin.vm");
            }
        } else {
            // If the user is NOT enabled, notify administrator(s).
            final String message = "Disabled user attempted to reset password: " + user.getUsername();
            log.warn(message);
            if (XDAT.getNotificationsPreferences().getSmtpEnabled()) {
                String body = XDAT.getNotificationsPreferences().getEmailMessageUnauthorizedDataAttempt();
                String type = "attempt. Someone attempted reset the password for the account " + user.getUsername() + ", but this account is currently disabled";
                body = body.replaceAll("TYPE", type);
                String userDetails = "You can contact the registered account owner through the email address: " + user.getEmail() + ".";
                body = body.replaceAll("USER_DETAILS", userDetails);
                AdminUtils.sendAdminEmail(user, "Possible hack attempt", body);
            }
            data.setMessage("Your account is currently disabled. Please contact the system administrator.");
            data.setScreenTemplate("Login.vm");
        }
    }

    @Override
    protected boolean isAuthorized(final RunData data) throws Exception {
        return true;
    }

    private String formatMessage(final UserI user, final String template, final String resetUrl, final String usernames) {
        return StringUtils.replaceEachRepeatedly(template, TOKENS, new String[]{TurbineUtils.GetFullServerPath() + "/app/template/Index.vm", TurbineUtils.GetSystemName(), StringUtils.defaultIfBlank(usernames, user.getUsername()), user.getFirstname(), user.getLastname(), _siteConfig.getAdminEmail(), _notifications.getHelpContactInfo(), resetUrl});
    }

    private boolean userAccountIsManagedExternally(final String username, final RunData data) {
        List<XdatUserAuth> userAuths = _userAuthService.getUsersByName(username);
        //The OpenID Plugin will create a user with auth_user which contains a | character
        //XNAT would replace the | character with _ and display the xdat_username in all screens
        if (userAuths == null || userAuths.isEmpty()) {
            userAuths = _userAuthService.getUsersByXdatUsername(username);
        }
        if (userAuths == null) {
            // let this fall through to downstream check for user existence
            return false;
        }
        // As long as there's a localdb login with the specified username, we're good.
        if (userAuths.stream().anyMatch(auth -> auth.getAuthMethod().equals(XdatUserAuthService.LOCALDB))) {
            return false;
        }

        final XdatUserAuth external = userAuths.stream().filter(auth -> !auth.getAuthMethod().equals(XdatUserAuthService.LOCALDB)).findFirst().orElse(null);
        if (external != null) {
            data.setMessage("The username or email you entered corresponds to an account that is managed externally " +
                            "and used in XNAT via " + external.getAuthMethod() + ". As such, credentials such as username " +
                            "and password are managed outside of XNAT. Contact your administrator for further assistance.");
            data.setScreenTemplate("Login.vm");
            return true;
        }

        // This really shouldn't ever happen.
        log.warn("Apparently a user with username {} exists, but isn't mapped to a user auth entry", username);
        return false;
    }

    private static final String[] TOKENS = {"SITE_URL", "SITE_NAME", "USER_USERNAME", "USER_FIRSTNAME", "USER_LASTNAME", "ADMIN_EMAIL", "HELP_EMAIL", "RESET_URL"};
    private static final long TWO_HOURS_IN_SECONDS = 7200L;

    private final XdatUserAuthService      _userAuthService;
    private final SiteConfigPreferences    _siteConfig;
    private final NotificationsPreferences _notifications;
    private final AliasTokenService        _aliasTokenService;
    private final MailService              _mailService;
    private final EmailRequestLogService   _requestLog;
}
