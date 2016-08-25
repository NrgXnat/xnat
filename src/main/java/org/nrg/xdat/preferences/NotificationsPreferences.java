package org.nrg.xdat.preferences;

import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.services.NrgEventService;
import org.nrg.mail.api.NotificationType;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xdat.XDAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@SuppressWarnings("unused")
@XmlRootElement
@NrgPreferenceBean(toolId = NotificationsPreferences.NOTIFICATIONS_TOOL_ID,
                   toolName = "XNAT Site Notifications Preferences",
                   description = "Manages notifications and email settings for the XNAT system.",
                   properties = "META-INF/xnat/preferences/notifications.properties",
                   strict = false)
public class NotificationsPreferences extends EventTriggeringAbstractPreferenceBean {
    public static final String NOTIFICATIONS_TOOL_ID = "notifications";

    @Autowired
    public NotificationsPreferences(final NrgPreferenceService preferenceService, final NrgEventService eventService, final ConfigPaths configFolderPaths) {
        super(preferenceService, eventService, configFolderPaths);
    }

    @NrgPreference(property = "notifications.helpContactInfo")
    public String getHelpContactInfo(){
        return getValue("notifications.helpContactInfo");
    }

    public void setHelpContactInfo(final String helpContactInfo) {
        try {
            set(helpContactInfo, "notifications.helpContactInfo");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.helpContactInfo': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue="Welcome to the SITE_NAME Web Archive!<br><br>You can now log on to the SITE_NAME at:<a href=\"SITE_URL\">SITE_URL</a><br><br>Your username is: USER_USERNAME<br><br><br>For support, contact the <a href=\"mailto:ADMIN_EMAIL?subject=SITE_NAME Assistance\">SITE_NAME Management </A>", property = "notifications.emailMessageUserRegistration")
    public String getEmailMessageUserRegistration(){
        return getValue("notifications.emailMessageUserRegistration");
    }

    public void setEmailMessageUserRegistration(final String emailMessageUserRegistration) {
        try {
            set(emailMessageUserRegistration, "notifications.emailMessageUserRegistration");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.emailMessageUserRegistration': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue="\nYou requested your username, which is: USER_USERNAME\n<br><br><br>Please login to the site for additional user information <a href=\"SITE_URL\">SITE_NAME</a>.\n", property = "notifications.emailMessageForgotUsernameRequest")
    public String getEmailMessageForgotUsernameRequest(){
        return getValue("notifications.emailMessageForgotUsernameRequest");
    }

    public void setEmailMessageForgotUsernameRequest(final String emailMessageForgotUsernameRequest) {
        try {
            set(emailMessageForgotUsernameRequest, "notifications.emailMessageForgotUsernameRequest");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.emailMessageForgotUsernameRequest': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue="Dear USER_FIRSTNAME USER_LASTNAME,\nPlease click this link to reset your password: RESET_URL <br/>\r\nThis link will expire in 24 hours.", property = "notifications.emailMessageForgotPasswordReset")
    public String getEmailMessageForgotPasswordReset(){
        return getValue("notifications.emailMessageForgotPasswordReset");
    }

    public void setEmailMessageForgotPasswordReset(final String emailMessageForgotPasswordReset) {
        try {
            set(emailMessageForgotPasswordReset, "notifications.emailMessageForgotPasswordReset");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.emailMessageForgotPasswordReset': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", property = "notifications.notifyAdminUserRegistration")
    public boolean getNotifyAdminUserRegistration(){
        return getBooleanValue("notifications.notifyAdminUserRegistration");
    }

    public void setNotifyAdminUserRegistration(final boolean notifyAdminUserRegistration) {
        try {
            setBooleanValue(notifyAdminUserRegistration, "notifications.notifyAdminUserRegistration");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.notifyAdminUserRegistration': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", property = "notifications.notifyAdminPipelineEmails")
    public boolean getNotifyAdminPipelineEmails(){
        return getBooleanValue("notifications.notifyAdminPipelineEmails");
    }

    public void setNotifyAdminPipelineEmails(final boolean notifyAdminPipelineEmails) {
        try {
            setBooleanValue(notifyAdminPipelineEmails, "notifications.notifyAdminPipelineEmails");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.notifyAdminPipelineEmails': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", property = "notifications.notifyAdminProjectAccessRequest")
    public boolean getNotifyAdminProjectAccessRequest(){
        return getBooleanValue("notifications.notifyAdminProjectAccessRequest");
    }

    public void setNotifyAdminProjectAccessRequest(final boolean notifyAdminProjectAccessRequest) {
        try {
            setBooleanValue(notifyAdminProjectAccessRequest, "notifications.notifyAdminProjectAccessRequest");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.notifyAdminProjectAccessRequest': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", property = "notifications.notifyAdminProjectOnSessionTransfer")
    public boolean getNotifyAdminSessionTransfer(){
        return getBooleanValue("notifications.notifyAdminProjectOnSessionTransfer");
    }

    public void setNotifyAdminSessionTransfer(final boolean notifyAdminProjectOnSessionTransfer) {
        try {
            setBooleanValue(notifyAdminProjectOnSessionTransfer, "notifications.notifyAdminProjectOnSessionTransfer");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.notifyAdminProjectOnSessionTransfer': something is very wrong here.", e);
        }
    }

    @NrgPreference(property = "notifications.emailRecipientErrorMessages")
    public String getEmailRecipientErrorMessages(){
        return XDAT.getSubscriberEmailsListAsString(NotificationType.Error);
    }

    public void setEmailRecipientErrorMessages(final String emailRecipientErrorMessages) {
        XDAT.replaceSubscriberList(emailRecipientErrorMessages, NotificationType.Error, getEmailAllowNonuserSubscribers());
    }

    @NrgPreference(property = "notifications.emailRecipientIssueReports")
    public String getEmailRecipientIssueReports(){
        return XDAT.getSubscriberEmailsListAsString(NotificationType.Issue);
    }

    public void setEmailRecipientIssueReports(final String emailRecipientIssueReports) {
        XDAT.replaceSubscriberList(emailRecipientIssueReports, NotificationType.Issue, getEmailAllowNonuserSubscribers());
    }

    @NrgPreference(property = "notifications.emailRecipientNewUserAlert")
    public String getEmailRecipientNewUserAlert(){
        return XDAT.getSubscriberEmailsListAsString(NotificationType.NewUser);
    }

    public void setEmailRecipientNewUserAlert(final String emailRecipientNewUserAlert) {
        XDAT.replaceSubscriberList(emailRecipientNewUserAlert, NotificationType.NewUser, getEmailAllowNonuserSubscribers());
    }

    @NrgPreference(property = "notifications.emailRecipientUpdate")
    public String getEmailRecipientUpdate(){
        return XDAT.getSubscriberEmailsListAsString(NotificationType.Update);
    }

    public void setEmailRecipientUpdate(final String emailRecipientUpdate) {
        XDAT.replaceSubscriberList(emailRecipientUpdate, NotificationType.Update, getEmailAllowNonuserSubscribers());
    }

    @NrgPreference(defaultValue = "{'host':'localhost','port':'25'}")
    public Map<String, String> getSmtpServer() {
        return getMapValue("smtpServer");
    }

    public void setSmtpServer(final Map<String, String> smtpServer) {
        try {
            setMapValue("smtpServer", smtpServer);
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'smtpServer': something is very wrong here.", e);
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

    @NrgPreference(defaultValue = "true", property = "smtp.enabled")
    public boolean getSmtpEnabled() {
        return getBooleanValue("smtp.enabled");
    }

    public void setSmtpEnabled(final boolean smtpEnabled) {
        try {
            setBooleanValue(smtpEnabled, "smtp.enabled");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'smtp.enabled': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", property = "mail.smtp.auth")
    public boolean getSmtpAuth() {
        return getBooleanValue("mail.smtp.auth");
    }

    public void setSmtpAuth(final boolean smtpAuth) {
        try {
            setBooleanValue(smtpAuth, "mail.smtp.auth");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'mail.smtp.auth': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", property = "mail.smtp.starttls.enable")
    public boolean getSmtpStartTls() {
        return getBooleanValue("mail.smtp.starttls.enable");
    }

    public void setSmtpStartTls(final boolean smtpStartTls) {
        try {
            setBooleanValue(smtpStartTls, "mail.smtp.starttls.enable");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'mail.smtp.starttls.enable': something is very wrong here.", e);
        }
    }

    @NrgPreference(property = "mail.smtp.ssl.trust")
    public String getSmtpSSLTrust() {
        return getValue("mail.smtp.ssl.trust");
    }

    public void setSmtpSSLTrust(final String smtpSSLTrust) {
        try {
            set(smtpSSLTrust, "mail.smtp.ssl.trust");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'mail.smtp.ssl.trust': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "XNAT", property = "emailPrefix")
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

    @NrgPreference(defaultValue = "localhost", property = "host")
    public String getHostname() {
        return getValue("host");
    }

    public void setHostname(final String host) {
        try {
            set(host, "host");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'host': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "25", property = "port")
    public int getPort() {
        return getIntegerValue("port");
    }

    public void setPort(final int port) {
        try {
            setIntegerValue(port, "port");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'port': something is very wrong here.", e);
        }
    }

    @NrgPreference(property = "username")
    public String getUsername() {
        return getValue("username");
    }

    public void setUsername(final String username) {
        try {
            set(username, "username");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'username': something is very wrong here.", e);
        }
    }

    @NrgPreference(property = "password")
    public String getPassword() {
        return getValue("password");
    }

    public void setPassword(final String password) {
        try {
            set(password, "password");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'password': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "smtp", property = "protocol")
    public String getProtocol() {
        return getValue("protocol");
    }

    public void setProtocol(final String protocol) {
        try {
            set(protocol, "protocol");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'protocol': something is very wrong here.", e);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(NotificationsPreferences.class);

}
