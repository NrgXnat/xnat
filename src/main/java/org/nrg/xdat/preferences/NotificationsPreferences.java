/*
 * core: org.nrg.xdat.preferences.NotificationsPreferences
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.preferences;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XnatMixIn;
import org.nrg.framework.beans.ProxiedBeanMixIn;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.mail.api.NotificationType;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;

@SuppressWarnings("unused")
@XmlRootElement
@NrgPreferenceBean(toolId = NotificationsPreferences.NOTIFICATIONS_TOOL_ID,
        toolName = "XNAT Site Notifications Preferences",
        description = "Manages notifications and email settings for the XNAT system.",
        properties = "META-INF/xnat/preferences/notifications.properties",
        strict = false)
@XnatMixIn(ProxiedBeanMixIn.class)
public class NotificationsPreferences extends EventTriggeringAbstractPreferenceBean {
    public static final String NOTIFICATIONS_TOOL_ID = "notifications";

    private static final String USER_REG_EMAIL        = "<p>Dear USER_FIRSTNAME USER_LASTNAME,</p> <p>Welcome to the SITE_NAME Web Archive!</p> <p>You can now log on to the SITE_NAME at: SITE_LINK</p><p>Your username is: USER_USERNAME</p> <p>For support, contact the ADMIN_MAIL_LINK</p>";
    private static final String FORGOT_USERNAME_EMAIL = "<p>You requested your username, which is: USER_USERNAME</p>\n <p>Please login to the site for additional user information SITE_LINK.</p>\n";
    private static final String FORGOT_PASSWORD_EMAIL = "<p>Dear USER_FIRSTNAME USER_LASTNAME,</p>\n<p>Please click this link to reset your password: RESET_LINK</p> \n<p>This link will expire in 24 hours.</p>";
    private static final String NEW_USER_VERIFICATION_EMAIL = "<p>USER_FIRSTNAME USER_LASTNAME, </p>\n<p>We received a request to register an account for you on XNAT. If you did not make this request, you can safely ignore this email. </p>\n" +
            "<p>If you would like to register, please confirm your email address by clicking this link within the next 24 hours: VERIFY_URL </p>\n" +
            "<p>ENABLED_MESSAGE</p>\n" +
            "<p>To request a new email verification link, please click this link and select \"Resend email verification\": FORGOT_LOGIN_URL</p>";
    private static final String REQUEST_PROJECT_ACCESS_EMAIL = "<p>Hello,</p>\n" +
            "<p>We received a request to access the PROJECT_NAME project from a user on SITE_NAME as a(n) RQ_ACCESS_LEVEL. Granting this kind of access in this project will mean the user can LIST_PERMISSIONS</p>\n" +
            "<p>Login: USER_USERNAME<br>\nEmail: USER_EMAIL<br>\nFirstname: USER_FIRSTNAME<br>\nLastname: USER_LASTNAME<br>Comments: RQA_COMMENTS<br>\nTo approve or deny this project access request, please click the following link: ACCESS_URL</p>\n" +
            "<p>The SITE_NAME team.<br>\nSITE_LINK <br>\nADMIN_MAIL_LINK</p>";
    private static final String PROJECT_ACCESS_APPROVAL_EMAIL = "<p>Hello,</p>\n" + "<p>You have been granted access to the PROJECT_NAME project as a member of the RQ_ACCESS_LEVEL group.</p>\n" +
            "<p>To proceed to SITE_LINK and begin working with this project, please click the following link: ACCESS_URL</p>\n" +
            "<p>The SITE_NAME team.<br>\nSITE_LINK <br>\nADMIN_MAIL_LINK </p>";
    private static final String PROJECT_ACCESS_DENIAL_EMAIL = "<p>We regret to inform you that your request to access the PROJECT_NAME project has been denied.  Please consult the project manager for additional details at USER_EMAIL.</p>\n" +
            "<p>The SITE_NAME team.<br>\nSITE_LINK <br>\nADMIN_MAIL_LINK</p>";
    private static final String INVITE_PROJECT_ACCESS_EMAIL = "<p>Hello,</p>\n" + "<p>You have been invited to join the PROJECT_NAME project on SITE_NAME by USER_FIRSTNAME USER_LASTNAME. If you were not expecting to receive this invitation, you can safely ignore this email.\n</p>" +
            "<p>To accept this invitation and begin working in this project, please click the following link: ACCEPT_URL \n<br />\n<br /></p>" + "<p>The SITE_NAME team.\n<br />SITE_LINK \n<br />ADMIN_MAIL_LINK</p>";
    private static final String DISABLED_USER_VERIFICATION_EMAIL = "<p>Expired User Reverified</p>\n" + "<ul style=\"list-style-type:none;\"> <li>Date: DATE_INPUT</li> <li>Site: SITE_NAME</li> <li>Host: SITE_LINK</li> <li>Username: USER_USERNAME</li> <li>First: USER_FIRSTNAME</li> <li>Last: USER_LASTNAME</li></ul>"
            + "ENABLED_MESSAGE";
    private static final String ERROR_EMAIL= "<p>Error Thrown:</p>\n <ul style=\"list-style-type:none;\"> <li>Host: SITE_NAME</li> <li>User: USER_LOGIN (USER_USERNAME USER_LASTNAME)</li> <li>Time: ERROR_TIME</li> <li>Error: ERROR_MESSAGE</li> </ul>";
    private static final String NEW_USER_NOTIFICATION_EMAIL = "<p>New User Created</p>\n" + "<ul style=\"list-style-type:none;\"> <li>Time: TIME</li> <li>Site: SITE_NAME</li> <li>Host: SITE_LINK</li> <li>Username: USER_USERNAME</li> <li>First: USER_FIRSTNAME</li> <li>Last: USER_LASTNAME</li> <li>Phone: USER_PHONE</li> <li>Lab: LAB_NAME</li> <li>Email: USER_EMAIL</li> </ul>" +
            "<p>This account has been created and automatically enabled based on the current system configuration.</p>\n" + "PROJECT_ACCESS_REQUESTS" +
            "<p> REVIEW_LINK </p>\n" +
            "<p>User Comments: USER_COMMENTS</p>";
    private static final String NEW_USER_REQUEST_EMAIL = "<p>New User Request</p>\n" + "<ul style=\"list-style-type:none;\"> <li> Time: TIME</li> <li>Site: SITE_NAME</li> <li>Host: SITE_LINK</li> <li>Username: USER_USERNAME</li> <li>First: USER_FIRSTNAME</li> <li>Last: USER_LASTNAME</li> <li>Phone: USER_PHONE</li> <li>Lab: LAB_NAME</li> <li>Email: USER_EMAIL</li> </ul>" +
            "<p>This account has been created but will be disabled until you enable the account. You must log in before enabling the account.</p>\n" + "<p>PROJECT_ACCESS_REQUESTS</p>\n" +
            "<p> REVIEW_LINK </p>\n" +
            "<p>User Comments: USER_COMMENTS</p>";
    private static final String PIPELINE_AUTORUN_SUCCESS_EMAIL = "<p>Dear USER_FIRSTNAME USER_LASTNAME,</p>\n <p>The following session was archived in SITE_NAME <ul style=\"list-style-type:none;\"> <li> Project: PROJECT_NAME</li> <li>Subject: SUBJECT_NAME</li> <li>Session: EXPERIMENT_NAME</li> </ul>" +
            "<p>Additional details for this session are available SUCCESS_URL.\n<br>SITE_NAME team</p>";
    private static final String PIPELINE_EMAIL_DEFAULT_SUCCESS = "<p>Dear USER_FIRSTNAME USER_LASTNAME,</p>\n <p>PIPELINE_NAME has completed without errors for EXPERIMENT_NAME.</p>\n <p>Details are available at SUCCESS_URL. <br>\nSITE_NAME team</p>";
    private static final String PIPELINE_EMAIL_DEFAULT_FAILURE = "<p>The pipeline PIPELINE_NAME encountered an error.</p> <ul style=\"list-style-type:none;\"> <li>Project: PROJECT_NAME</li> <li>Experiment: EXPERIMENT_NAME</li> <li>Pipeline Step: PIPELINE_STEP </li> </ul>" +
            "<p>The SITE_NAME technical team is aware of the issue and will notify you when it has been resolved.</p>\n <p>We appreciate your patience. Please contact CONTACT_EMAIL with questions or concerns.</p>\n" +
            "<p>ATTACHMENTS_STATEMENT<br>\nPIPELINE_PARAMETERS<br>\nSTDOUT<br>\nSTDERR</p>";
    private static final String BATCH_WORKFLOW_COMPLETE_EMAIL = "<p>Dear USER_FIRSTNAME USER_LASTNAME,</p>\n <p>The following batch procedure has been completed: \n<br>PROCESS_NAME </p>\n" +
            "<p>NUMBER_MESSAGES successful transfer(s).\n<br>MESSAGES_LIST \n<br>ERRORS_LIST \n<br>Details for this project are available at SITE_LINK.</p>\n" +
            "<p>The SITE_NAME team.<br>\nSITE_LINK <br>\n ADMIN_MAIL_LINK </p>";
    private static final String UNAUTHORIZED_DATA_ATTEMPT_EMAIL = "<p>Unauthorized access TYPE USER_DETAILS prevented</p>";
    private static final String EMAIL_CHANGE_REQUEST_EMAIL = "<p>A request was made to change the email address for the user with username USER_USERNAME to NEW_EMAIL. If you did not make this request, someone else may have gotten access to your account and you should contact the site administrator: ADMIN_MAIL_LINK.</p>";
    private static final String VERIFY_EMAIL_CHANGE_REQUEST_EMAIL = "<p>A request was made to change the email address for the user with username USER_USERNAME to this address. If you did not make this request, you can ignore this email. If you made this request and wish to have this change take effect, please log into your account and click CHANGE_EMAIL_LINK.</p>";
    private static final String EMAIL_ADDRESS_CHANGED_EMAIL = "<p>Your email address was successfully changed to NEW_EMAIL.</p>";
    private static final String SYSTEM_PATH_ERROR = "<p>The following system path errors have been discovered:\n<br>ERRORS_LIST</p>";
    private static final String UPLOAD_BY_REFERENCE_SUCCESS = "<p>The upload by reference requested by USER_USERNAME has finished successfully.\n<br>DUPLICATES_LIST</p>";
    private static final String UPLOAD_BY_REFERENCE_ERROR = "<p>The upload by reference requested by USER_USERNAME has encountered an error.</p>\n <p>Please contact your IT staff or the application logs for more information.</p>";
    private static final String DATA_ALERT_CUSTOM_MESSAGE = "<p>Hello, </p> <p>USER_FIRSTNAME USER_LASTNAME thought you might be interested in a data set contained in the SITE_NAME. Please follow REQUEST_LINK to view the data.</p>\n" +
            "<p>Message from sender:<br>\n" + "SENDER_MESSAGE<br>\n</p> <p>This email was sent by the SITE_LINK data management system on TIME_SENT. If you have questions or concerns, please contact HELP_CONTACT</p>.";

    @Autowired
    public NotificationsPreferences(final NrgPreferenceService preferenceService, final NrgEventServiceI eventService, final ConfigPaths configPaths, final OrderedProperties initPrefs) {
        super(preferenceService, eventService, configPaths, initPrefs);
    }

    /**
     * This method and the {@link #setSmtpServer(SmtpServer)} method are basically convenience functions that make it
     * easier to deal with the various individual SMTP server settings. Changes in the returned {@link SmtpServer}
     * object will have no effect on the SMTP settings in the preferences unless you set the properties by calling the
     * {@link #setSmtpServer(SmtpServer)} method.
     *
     * @return The current SMTP settings.
     */
    public SmtpServer getSmtpServer() {
        return new SmtpServer(this);
    }

    /**
     * This method and the {@link #getSmtpServer()} method are basically convenience functions that make it easier to
     * deal with the various individual SMTP server settings. Any changes in settings in the {@link SmtpServer} object
     * set here will be updated for the corresponding settings in the notifications preferences.
     *
     * @param smtpServer The SMTP values to set.
     */
    public void setSmtpServer(final SmtpServer smtpServer) {
        setSmtpHostname(smtpServer.getHostname());
        setSmtpPort(smtpServer.getPort());
        setSmtpProtocol(smtpServer.getProtocol());
        setSmtpUsername(smtpServer.getUsername());
        setSmtpPassword(smtpServer.getPassword());
        setSmtpAuth(smtpServer.getSmtpAuth());
        setSmtpStartTls(smtpServer.getSmtpStartTls());
        setSmtpSslTrust(smtpServer.getSmtpSslTrust());
        setSmtpMailProperties(smtpServer.getMailProperties());
    }

    public String getEmailRecipientErrorMessages() {
        return XDAT.getSubscriberEmailsListAsString(NotificationType.Error);
    }

    public void setEmailRecipientErrorMessages(final String emailRecipientErrorMessages) {
        XDAT.replaceSubscriberList(emailRecipientErrorMessages, NotificationType.Error, getEmailAllowNonuserSubscribers());
    }

    public String getEmailRecipientIssueReports() {
        return XDAT.getSubscriberEmailsListAsString(NotificationType.Issue);
    }

    public void setEmailRecipientIssueReports(final String emailRecipientIssueReports) {
        XDAT.replaceSubscriberList(emailRecipientIssueReports, NotificationType.Issue, getEmailAllowNonuserSubscribers());
    }

    public String getEmailRecipientNewUserAlert() {
        return XDAT.getSubscriberEmailsListAsString(NotificationType.NewUser);
    }

    public void setEmailRecipientNewUserAlert(final String emailRecipientNewUserAlert) {
        XDAT.replaceSubscriberList(emailRecipientNewUserAlert, NotificationType.NewUser, getEmailAllowNonuserSubscribers());
    }

    public String getEmailRecipientUpdate() {
        return XDAT.getSubscriberEmailsListAsString(NotificationType.Update);
    }

    public void setEmailRecipientUpdate(final String emailRecipientUpdate) {
        XDAT.replaceSubscriberList(emailRecipientUpdate, NotificationType.Update, getEmailAllowNonuserSubscribers());
    }

    @NrgPreference(defaultValue = "localhost", aliases = {"host", "hostname"})
    public String getSmtpHostname() {
        return getValue("smtpHostname");
    }

    public void setSmtpHostname(final String smtpHostname) {
        try {
            set(smtpHostname, "smtpHostname");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'smtpHostname': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "25", aliases = "port")
    public int getSmtpPort() {
        try {
            return getIntegerValue("smtpPort");
        } catch (NumberFormatException e) {
            // If there's a bad port set, just set it to the default value.
            setSmtpPort(25);
            return 25;
        }
    }

    public void setSmtpPort(final int smtpPort) {
        try {
            setIntegerValue(smtpPort, "smtpPort");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'smtpPort': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "smtp", aliases = "protocol")
    public String getSmtpProtocol() {
        return getValue("smtpProtocol");
    }

    public void setSmtpProtocol(final String smtpProtocol) {
        try {
            set(smtpProtocol, "smtpProtocol");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'smtpProtocol': something is very wrong here.", e);
        }
    }

    @NrgPreference(aliases = "username")
    public String getSmtpUsername() {
        return getValue("smtpUsername");
    }

    public void setSmtpUsername(final String smtpUsername) {
        try {
            set(smtpUsername, "smtpUsername");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'smtpUsername': something is very wrong here.", e);
        }
    }

    @NrgPreference(aliases = "password")
    public String getSmtpPassword() {
        return getValue("smtpPassword");
    }

    public void setSmtpPassword(final String smtpPassword) {
        try {
            set(smtpPassword, "smtpPassword");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'smtpPassword': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "smtp.enabled")
    public boolean getSmtpEnabled() {
        return getBooleanValue("smtpEnabled");
    }

    public void setSmtpEnabled(final boolean smtpEnabled) {
        try {
            setBooleanValue(smtpEnabled, "smtpEnabled");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'smtpEnabled': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", aliases = "mail.smtp.auth")
    public boolean getSmtpAuth() {
        return getBooleanValue("smtpAuth");
    }

    public void setSmtpAuth(final boolean smtpAuth) {
        try {
            setBooleanValue(smtpAuth, "smtpAuth");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'smtpAuth': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", aliases = "mail.smtp.starttls.enable")
    public boolean getSmtpStartTls() {
        return getBooleanValue("smtpStartTls");
    }

    public void setSmtpStartTls(final boolean smtpStartTls) {
        try {
            setBooleanValue(smtpStartTls, "smtpStartTls");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'smtpStartTls': something is very wrong here.", e);
        }
    }

    @NrgPreference(aliases = "mail.smtp.ssl.trust")
    public String getSmtpSslTrust() {
        return getValue("smtpSslTrust");
    }

    public void setSmtpSslTrust(final String smtpSslTrust) {
        try {
            set(smtpSslTrust, "smtpSslTrust");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'smtpSslTrust': something is very wrong here.", e);
        }
    }

    @NrgPreference
    public Properties getSmtpMailProperties() {
        try {
            final String     value      = getValue("smtpMailProperties");
            final Properties properties = deserialize(StringUtils.defaultIfBlank(value, "{}"), Properties.class);
            if (getSmtpAuth()) {
                properties.setProperty(SmtpServer.SMTP_KEY_AUTH, "true");
                if (getSmtpStartTls()) {
                    properties.setProperty(SmtpServer.SMTP_KEY_STARTTLS_ENABLE, "true");
                }
                if (StringUtils.isNotBlank(getSmtpSslTrust())) {
                    properties.setProperty(SmtpServer.SMTP_KEY_SSL_TRUST, getSmtpSslTrust());
                }
            }
            return properties;
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred trying to deserialize the 'smtpMailProperties' preference", e);
        }
    }

    public void setSmtpMailProperties(final Properties properties) {
        setSmtpServer(new SmtpServer(properties));
    }

    @JsonIgnore
    public String getSmtpMailProperty(final String property) {
        return getSmtpServer().getMailProperty(property);
    }

    @JsonIgnore
    public String setSmtpMailProperty(final String property, final String value) {
        final SmtpServer smtpServer = getSmtpServer();
        final String     oldValue   = smtpServer.setMailProperty(property, value);
        setSmtpServer(smtpServer);
        return oldValue;
    }

    @JsonIgnore
    public String removeSmtpMailProperty(final String property) {
        final SmtpServer smtpServer = getSmtpServer();
        final String     oldValue   = smtpServer.removeMailProperty(property);
        setSmtpServer(smtpServer);
        return oldValue;
    }

    @NrgPreference
    public String getHelpContactInfo() {
        return getValue("helpContactInfo");
    }

    public void setHelpContactInfo(final String helpContactInfo) {
        try {
            set(helpContactInfo, "helpContactInfo");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'helpContactInfo': something is very wrong here.", e);
        }
    }

    public String replaceCommonAnchorTags(String body, UserI user) {
        String siteURL = TurbineUtils.GetFullServerPath();
        body = body.replaceAll("SITE_URL", siteURL);
        String siteUrLink = "<a href=\"" + siteURL + "\">" + siteURL + "</a>";
        body = body.replaceAll("SITE_LINK",siteUrLink);
        body = body.replaceAll("SITE_NAME",TurbineUtils.GetSystemName());

        body = body.replaceAll("ADMIN_EMAIL", XDAT.getSiteConfigPreferences().getAdminEmail());

        String adminEmailLink = "<a href=\"mailto:" + XDAT.getSiteConfigPreferences().getAdminEmail() + "?subject=" + TurbineUtils.GetSystemName() + " Assistance\">" + TurbineUtils.GetSystemName() + " Management </a>";
        body = body.replaceAll("ADMIN_MAIL_LINK",adminEmailLink);

        if (user != null) {
            body = body.replaceAll("USER_USERNAME", user.getUsername());
            body = body.replaceAll("USER_FIRSTNAME", user.getFirstname());
            body = body.replaceAll("USER_LASTNAME",user.getLastname());
            body = body.replaceAll("USER_EMAIL", user.getEmail());
        }
        return body;
    }

    public String replaceBackwardsCompatibleEmailInconsistencies(String body) {
        body = body.replaceAll("a href=\\\\", "a href=");
        body = body.replaceAll("\\\\\">", "\">");
        body = body.replaceAll("\\\\n", "");
        body = body.replaceAll("\\\\r", "");
        return body;
    }

    @NrgPreference(defaultValue = USER_REG_EMAIL)
    public String getEmailMessageUserRegistration() {
        return getValue("emailMessageUserRegistration");
    }

    public void setEmailMessageUserRegistration(final String emailMessageUserRegistration) {
        try {
            set(emailMessageUserRegistration, "emailMessageUserRegistration");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageUserRegistration': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageUserRegistration() {
        setEmailMessageUserRegistration(USER_REG_EMAIL);
    }

    @NrgPreference(defaultValue = FORGOT_USERNAME_EMAIL)
    public String getEmailMessageForgotUsernameRequest() {
        return getValue("emailMessageForgotUsernameRequest");
    }

    public void setEmailMessageForgotUsernameRequest(final String emailMessageForgotUsernameRequest) {
        try {
            set(emailMessageForgotUsernameRequest, "emailMessageForgotUsernameRequest");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageForgotUsernameRequest': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageForgotUsername() {
        setEmailMessageForgotUsernameRequest(FORGOT_USERNAME_EMAIL);
    }

    @NrgPreference(defaultValue = FORGOT_PASSWORD_EMAIL)
    public String getEmailMessageForgotPasswordReset() {
        return getValue("emailMessageForgotPasswordReset");
    }

    public void setEmailMessageForgotPasswordReset(final String emailMessageForgotPasswordReset) {
        try {
            set(emailMessageForgotPasswordReset, "emailMessageForgotPasswordReset");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageForgotPasswordReset': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageForgotPassword() {
        setEmailMessageForgotPasswordReset(FORGOT_PASSWORD_EMAIL);
    }

    @NrgPreference(defaultValue = NEW_USER_VERIFICATION_EMAIL)
    public String getEmailMessageNewUserVerification() {
        return getValue("emailMessageNewUserVerification");
    }

    public void setEmailMessageNewUserVerification(final String emailMessageNewUserVerification) {
        try {
            set(emailMessageNewUserVerification, "emailMessageNewUserVerification");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageNewUserVerification': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageNewUserVerification() {
        setEmailMessageNewUserVerification(NEW_USER_VERIFICATION_EMAIL);
    }

    @NrgPreference(defaultValue = REQUEST_PROJECT_ACCESS_EMAIL)
    public String getEmailMessageProjectRequestAccess() {
        return getValue("emailMessageProjectRequestAccess");
    }

    public void setEmailMessageProjectRequestAccess(final String emailMessageProjectRequestAccess) {
        try {
            set(emailMessageProjectRequestAccess, "emailMessageProjectRequestAccess");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageProjectRequestAccess': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageProjectRequestAccess() {
        setEmailMessageProjectRequestAccess(REQUEST_PROJECT_ACCESS_EMAIL);
    }

    @NrgPreference(defaultValue = PROJECT_ACCESS_APPROVAL_EMAIL)
    public String getEmailMessageProjectAccessApproval() {return getValue("emailMessageProjectAccessApproval"); }

    public void setEmailMessageProjectAccessApproval(final String emailMessageProjectAccessApproval) {
        try {
            set(emailMessageProjectAccessApproval, "emailMessageProjectAccessApproval");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageProjectAccessApproval': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageProjectAccessApproval() {
        setEmailMessageProjectAccessApproval(PROJECT_ACCESS_APPROVAL_EMAIL);
    }

    @NrgPreference(defaultValue = PROJECT_ACCESS_DENIAL_EMAIL)
    public String getEmailMessageProjectAccessDenial() {
        return getValue("emailMessageProjectAccessDenial");
    }

    public void setEmailMessageProjectAccessDenial(final String emailMessageProjectAccessDenial) {
        try {
            set(emailMessageProjectAccessDenial, "emailMessageProjectAccessDenial");
        } catch (InvalidPreferenceName e){
            _log.error("Invalid preference name 'emailMessageProjectAccessDenial': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageProjectAccessDenial() {
        setEmailMessageProjectAccessDenial(PROJECT_ACCESS_DENIAL_EMAIL);
    }

    @NrgPreference(defaultValue = INVITE_PROJECT_ACCESS_EMAIL)
    public String getEmailMessageInviteProjectAccess() {
        return getValue("emailMessageInviteProjectAccess");
    }

    public void setEmailMessageInviteProjectAccess( final String emailMessageInviteProjectAccess) {
        try {
            set(emailMessageInviteProjectAccess, "emailMessageInviteProjectAccess");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageInviteProjectAccess': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageInviteProjectAccess() {
        setEmailMessageInviteProjectAccess(INVITE_PROJECT_ACCESS_EMAIL);
    }
    
    @NrgPreference(defaultValue = DISABLED_USER_VERIFICATION_EMAIL)
    public String getEmailMessageDisabledUserVerification() { return getValue("emailMessageDisabledUserVerification"); }

    public void setEmailMessageDisabledUserVerification(final String emailMessageDisabledUserVerification) {
        try {
            set(emailMessageDisabledUserVerification, "emailMessageDisabledUserVerification");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageDisabledUserVerification': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageDisabledUserVerification() {
        setEmailMessageDisabledUserVerification(DISABLED_USER_VERIFICATION_EMAIL);
    }

    @NrgPreference(defaultValue = ERROR_EMAIL)
    public String getEmailMessageErrorMessage() { return getValue("emailMessageErrorMessage"); }

    public void setEmailMessageErrorMessage(final String emailMessageErrorMessage) {
        try {
            set(emailMessageErrorMessage, "emailMessageErrorMessage");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageErrorMessage': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageErrorMessage() {
        setEmailMessageErrorMessage(ERROR_EMAIL);
    }

    @NrgPreference(defaultValue = NEW_USER_NOTIFICATION_EMAIL)
    public String getEmailMessageNewUserNotification() { return getValue("emailMessageNewUserNotification"); }

    public void setEmailMessageNewUserNotification(final String emailMessageNewUserNotification) {
        try {
            set(emailMessageNewUserNotification, "emailMessageNewUserNotification");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageNewUserNotification': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageNewUserNotification() {
        setEmailMessageNewUserNotification(NEW_USER_NOTIFICATION_EMAIL);
    }

    @NrgPreference(defaultValue = NEW_USER_REQUEST_EMAIL)
    public String getEmailMessageNewUserRequest() { return getValue("emailMessageNewUserRequest"); }

    public void setEmailMessageNewUserRequest(final String emailMessageNewUserRequest) {
        try {
            set(emailMessageNewUserRequest, "emailMessageNewUserRequest");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageNewUserRequest': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageNewUserRequest() {
        setEmailMessageNewUserRequest(NEW_USER_REQUEST_EMAIL);
    }

    @NrgPreference(defaultValue = PIPELINE_AUTORUN_SUCCESS_EMAIL)
    public String getEmailMessagePipelineAutorunSuccess() { return getValue("emailMessagePipelineAutorunSuccess"); }

    public void setEmailMessagePipelineAutorunSuccess(final String emailMessagePipelineAutorunSuccess) {
        try {
            set(emailMessagePipelineAutorunSuccess, "emailMessagePipelineAutorunSuccess");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessagePipelineAutorunSuccess': something is very wrong here.", e);
        }
    }

    public void resetEmailMessagePipelineAutorunSuccess() {
        setEmailMessagePipelineAutorunSuccess(PIPELINE_AUTORUN_SUCCESS_EMAIL);
    }

    @NrgPreference(defaultValue = PIPELINE_EMAIL_DEFAULT_SUCCESS)
    public String getEmailMessagePipelineDefaultSuccess() { return getValue("emailMessagePipelineDefaultSuccess"); }

    public void setEmailMessagePipelineDefaultSuccess(final String emailMessagePipelineDefaultSuccess) {
        try {
            set(emailMessagePipelineDefaultSuccess, "emailMessagePipelineDefaultSuccess");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessagePipelineDefaultSuccess': something is very wrong here.", e);
        }
    }

    public void resetEmailMessagePipelineDefaultSuccess() {
        setEmailMessagePipelineDefaultSuccess(PIPELINE_EMAIL_DEFAULT_SUCCESS);
    }

    @NrgPreference(defaultValue = PIPELINE_EMAIL_DEFAULT_FAILURE)
    public String getEmailMessagePipelineDefaultFailure() { return getValue("emailMessagePipelineDefaultFailure"); }

    public void setEmailMessagePipelineDefaultFailure(final String emailMessagePipelineDefaultFailure) {
        try {
            set(emailMessagePipelineDefaultFailure, "emailMessagePipelineDefaultFailure");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessagePipelineDefaultFailure': something is very wrong here.", e);
        }
    }

    public void resetEmailMessagePipelineDefaultFailure() {
        setEmailMessagePipelineDefaultFailure(PIPELINE_EMAIL_DEFAULT_FAILURE);
    }

    @NrgPreference(defaultValue = BATCH_WORKFLOW_COMPLETE_EMAIL)
    public String getEmailMessageBatchWorkflowComplete() { return getValue("emailMessageBatchWorkflowComplete"); }

    public void setEmailMessageBatchWorkflowComplete(final String emailMessageBatchWorkflowComplete) {
        try {
            set(emailMessageBatchWorkflowComplete, "emailMessageBatchWorkflowComplete");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageBatchWorkflowComplete': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageBatchWorkflowComplete() {
        setEmailMessageBatchWorkflowComplete(BATCH_WORKFLOW_COMPLETE_EMAIL);
    }

    @NrgPreference(defaultValue = UNAUTHORIZED_DATA_ATTEMPT_EMAIL)
    public String getEmailMessageUnauthorizedDataAttempt() { return getValue("emailMessageUnauthorizedDataAttempt"); }

    public void setEmailMessageUnauthorizedDataAttempt(final String emailMessageUnauthorizedDataAttempt) {
        try {
            set(emailMessageUnauthorizedDataAttempt, "emailMessageUnauthorizedDataAttempt");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageUnauthorizedDataAttempt': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageUnauthorizedDataAttempt() {
        setEmailMessageUnauthorizedDataAttempt(UNAUTHORIZED_DATA_ATTEMPT_EMAIL);
    }

    @NrgPreference(defaultValue = EMAIL_CHANGE_REQUEST_EMAIL)
    public String getEmailMessageEmailAddressChangeRequest() { return getValue("emailMessageEmailAddressChangeRequest"); }

    public void setEmailMessageEmailAddressChangeRequest(final String emailMessageEmailAddressChangeRequest) {
        try {
            set(emailMessageEmailAddressChangeRequest, "emailMessageEmailAddressChangeRequest");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageEmailAddressChangeRequest': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageEmailAddressChangeRequest() {
        setEmailMessageEmailAddressChangeRequest(EMAIL_CHANGE_REQUEST_EMAIL);
    }

    @NrgPreference(defaultValue = VERIFY_EMAIL_CHANGE_REQUEST_EMAIL)
    public String getEmailMessageVerifyEmailChangeRequest() { return getValue("emailMessageVerifyEmailChangeRequest"); }

    public void setEmailMessageVerifyEmailChangeRequest(final String emailMessageVerifyEmailChangeRequest) {
        try {
            set(emailMessageVerifyEmailChangeRequest, "emailMessageVerifyEmailChangeRequest");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageVerifyEmailChangeRequest': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageVerifyEmailChangeRequest() {
        setEmailMessageVerifyEmailChangeRequest(VERIFY_EMAIL_CHANGE_REQUEST_EMAIL);
    }

    @NrgPreference(defaultValue = EMAIL_ADDRESS_CHANGED_EMAIL)
    public String getEmailMessageAddressChanged() { return getValue("emailMessageAddressChanged"); }

    public void setEmailMessageAddressChanged(final String emailMessageAddressChanged) {
        try {
            set(emailMessageAddressChanged, "emailMessageAddressChanged");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageAddressChanged': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageAddressChanged() {
        setEmailMessageAddressChanged(EMAIL_ADDRESS_CHANGED_EMAIL);
    }

    @NrgPreference(defaultValue = SYSTEM_PATH_ERROR)
    public String getEmailMessageSystemPathError() { return getValue("emailMessageSystemPathError"); }

    public void setEmailMessageSystemPathError(final String emailMessageSystemPathError) {
        try {
            set(emailMessageSystemPathError, "emailMessageSystemPathError");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageSystemPathError': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageSystemPathError() {
        setEmailMessageSystemPathError(SYSTEM_PATH_ERROR);
    }

    @NrgPreference(defaultValue = UPLOAD_BY_REFERENCE_SUCCESS)
    public String getEmailMessageUploadByReferenceSuccess() { return getValue("emailMessageUploadByReferenceSuccess"); }

    public void setEmailMessageUploadByReferenceSuccess(final String emailMessageUploadByReferenceSuccess) {
        try {
            set(emailMessageUploadByReferenceSuccess, "emailMessageUploadByReferenceSuccess");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageUploadByReferenceSuccess': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageUploadByReferenceSuccess() {
        setEmailMessageUploadByReferenceSuccess(UPLOAD_BY_REFERENCE_SUCCESS);
    }

    @NrgPreference(defaultValue = UPLOAD_BY_REFERENCE_ERROR)
    public String getEmailMessageUploadByReferenceFailure() { return getValue("emailMessageUploadByReferenceFailure"); }

    public void setEmailMessageUploadByReferenceFailure(final String emailMessageUploadByReferenceFailure) {
        try {
            set(emailMessageUploadByReferenceFailure, "emailMessageUploadByReferenceFailure");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageUploadByReferenceFailure': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageUploadByReferenceFailure() {
        setEmailMessageUploadByReferenceFailure(UPLOAD_BY_REFERENCE_ERROR);
    }

    @NrgPreference(defaultValue = DATA_ALERT_CUSTOM_MESSAGE)
    public String getEmailMessageDataAlertCustom() { return getValue("emailMessageDataAlertCustom"); }

    public void setEmailMessageDataAlertCustom(final String emailMessageDataAlertCustom) {
        try {
            set(emailMessageDataAlertCustom, "emailMessageDataAlertCustom");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailMessageDataAlertCustom': something is very wrong here.", e);
        }
    }

    public void resetEmailMessageDataAlertCustom() {
        setEmailMessageDataAlertCustom(DATA_ALERT_CUSTOM_MESSAGE);
    }

    @NrgPreference(defaultValue = "false")
    public boolean getNotifyAdminUserRegistration() {
        return getBooleanValue("notifyAdminUserRegistration");
    }

    public void setNotifyAdminUserRegistration(final boolean notifyAdminUserRegistration) {
        try {
            setBooleanValue(notifyAdminUserRegistration, "notifyAdminUserRegistration");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifyAdminUserRegistration': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getNotifyAdminPipelineEmails() {
        return getBooleanValue("notifyAdminPipelineEmails");
    }

    public void setNotifyAdminPipelineEmails(final boolean notifyAdminPipelineEmails) {
        try {
            setBooleanValue(notifyAdminPipelineEmails, "notifyAdminPipelineEmails");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifyAdminPipelineEmails': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getNotifyAdminProjectAccessRequest() {
        return getBooleanValue("notifyAdminProjectAccessRequest");
    }

    public void setNotifyAdminProjectAccessRequest(final boolean notifyAdminProjectAccessRequest) {
        try {
            setBooleanValue(notifyAdminProjectAccessRequest, "notifyAdminProjectAccessRequest");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifyAdminProjectAccessRequest': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getNotifyAdminSessionTransfer() {
        return getBooleanValue("notifyAdminSessionTransfer");
    }

    public void setNotifyAdminSessionTransfer(final boolean notifyAdminProjectOnSessionTransfer) {
        try {
            setBooleanValue(notifyAdminProjectOnSessionTransfer, "notifyAdminSessionTransfer");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifyAdminSessionTransfer': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getCopyAdminOnPageEmails() {
        return getBooleanValue("copyAdminOnPageEmails");
    }

    public void setCopyAdminOnPageEmails(final boolean copyAdminOnPageEmails) {
        try {
            setBooleanValue(copyAdminOnPageEmails, "copyAdminOnPageEmails");
        } catch (InvalidPreferenceName invalidPreferenceName) {
            _log.error("Invalid preference name 'copyAdminOnPageEmails': something is very wrong here.", invalidPreferenceName);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getCopyAdminOnNotifications() {
        return getBooleanValue("copyAdminOnNotifications");
    }

    public void setCopyAdminOnNotifications(final boolean copyAdminOnNotifications) {
        try {
            setBooleanValue(copyAdminOnNotifications, "copyAdminOnNotifications");
        } catch (InvalidPreferenceName invalidPreferenceName) {
            _log.error("Invalid preference name 'copyAdminOnNotifications': something is very wrong here.", invalidPreferenceName);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getEmailAllowNonuserSubscribers() {
        return getBooleanValue("emailAllowNonuserSubscribers");
    }

    public void setEmailAllowNonuserSubscribers(final boolean emailAllowNonuserSubscribers) {
        try {
            setBooleanValue(emailAllowNonuserSubscribers, "emailAllowNonuserSubscribers");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailAllowNonuserSubscribers': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "XNAT")
    public String getEmailPrefix() {
        return getValue("emailPrefix");
    }

    public void setEmailPrefix(final String emailPrefix) {
        try {
            set(emailPrefix, "emailPrefix");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailPrefix': something is very wrong here.", e);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(NotificationsPreferences.class);
}
