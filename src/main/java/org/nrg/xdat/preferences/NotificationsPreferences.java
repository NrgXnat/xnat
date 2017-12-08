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
import org.nrg.framework.services.NrgEventService;
import org.nrg.framework.utilities.OrderedProperties;
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
import java.io.IOException;
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

    private static final String USER_REG_EMAIL        = "Welcome to the SITE_NAME Web Archive!<br><br>You can now log on to the SITE_NAME at:<a href=\"SITE_URL\">SITE_URL</a><br><br>Your username is: USER_USERNAME<br><br><br>For support, contact the <a href=\"mailto:ADMIN_EMAIL?subject=SITE_NAME Assistance\">SITE_NAME Management </A>";
    private static final String FORGOT_USERNAME_EMAIL = "\nYou requested your username, which is: USER_USERNAME\n<br><br><br>Please login to the site for additional user information <a href=\"SITE_URL\">SITE_NAME</a>.\n";
    private static final String FORGOT_PASSWORD_EMAIL = "Dear USER_FIRSTNAME USER_LASTNAME,\nPlease click this link to reset your password: RESET_URL <br/>\r\nThis link will expire in 24 hours.";

    @Autowired
    public NotificationsPreferences(final NrgPreferenceService preferenceService, final NrgEventService eventService, final ConfigPaths configPaths, final OrderedProperties initPrefs) {
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
