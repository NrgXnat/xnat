package org.nrg.xdat.preferences;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

@SuppressWarnings("unused")
public class SmtpServer {
    public static final String SMTP_KEY_AUTH            = "mail.smtp.auth";
    public static final String SMTP_KEY_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    public static final String SMTP_KEY_SSL_TRUST       = "mail.smtp.ssl.trust";

    private static final String SMTP_KEY_HOSTNAME      = "hostname";
    private static final String SMTP_KEY_PORT          = "port";
    private static final String SMTP_KEY_PROTOCOL      = "protocol";
    private static final String SMTP_KEY_USERNAME      = "username";
    private static final String SMTP_KEY_PASSWORD      = "password";
    private static final String SMTP_KEY_SMTP_HOSTNAME = "smtpHostname";
    private static final String SMTP_KEY_SMTP_PORT     = "smtpPort";
    private static final String SMTP_KEY_SMTP_PROTOCOL = "smtpProtocol";
    private static final String SMTP_KEY_SMTP_USERNAME = "smtpUsername";
    private static final String SMTP_KEY_SMTP_PASSWORD = "smtpPassword";

    public SmtpServer() {
        // Default constructor.
    }

    public SmtpServer(final SmtpServer smtpServer) {
        this(smtpServer.getHostname(), smtpServer.getPort(), smtpServer.getProtocol(), smtpServer.getUsername(), smtpServer.getPassword(), smtpServer.getMailProperties());
    }

    public SmtpServer(final Properties properties) {
        setMailProperties(properties);
    }

    public SmtpServer(final NotificationsPreferences preferences) {
        this(preferences.getSmtpHostname(), preferences.getSmtpPort(), preferences.getSmtpProtocol(), preferences.getSmtpUsername(), preferences.getSmtpPassword(), preferences.getSmtpMailProperties());
    }

    public SmtpServer(final String hostname, final int port, final String protocol, final String username, final String password, final Properties properties) {
        setHostname(hostname);
        setPort(port);
        setProtocol(protocol);
        setUsername(username);
        setPassword(password);
        setMailProperties(properties);
    }

    public String getHostname() {
        return _hostname;
    }

    public void setHostname(final String hostname) {
        _hostname = hostname;
    }

    public int getPort() {
        return _port;
    }

    public void setPort(final int port) {
        _port = port;
    }

    public String getProtocol() {
        return _protocol;
    }

    public void setProtocol(final String protocol) {
        _protocol = protocol;
    }

    public String getUsername() {
        return _username;
    }

    public void setUsername(final String username) {
        _username = username;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(final String password) {
        _password = password;
    }

    public boolean getSmtpAuth() {
        return Boolean.parseBoolean(_mailProperties.getProperty(SMTP_KEY_AUTH));
    }

    public void setSmtpAuth(final boolean smtpAuth) {
        _mailProperties.setProperty(SMTP_KEY_AUTH, Boolean.toString(smtpAuth));
    }

    public void setSmtpAuth(final String smtpAuth) {
        setSmtpAuth(Boolean.parseBoolean(smtpAuth));
    }

    public boolean getSmtpStartTls() {
        return Boolean.parseBoolean(_mailProperties.getProperty(SMTP_KEY_STARTTLS_ENABLE));
    }

    public void setSmtpStartTls(final boolean smtpStartTls) {
        _mailProperties.setProperty(SMTP_KEY_STARTTLS_ENABLE, Boolean.toString(smtpStartTls));
    }

    public void setSmtpStartTls(final String smtpStartTls) {
        setSmtpStartTls(Boolean.parseBoolean(smtpStartTls));
    }

    public String getSmtpSslTrust() {
        return _mailProperties.getProperty(SMTP_KEY_SSL_TRUST);
    }

    public void setSmtpSslTrust(final String smtpSslTrust) {
        _mailProperties.setProperty(SMTP_KEY_SSL_TRUST, smtpSslTrust);
    }

    public Properties getMailProperties() {
        return new Properties(_mailProperties);
    }

    public void setMailProperties(final Properties properties) {
        if (properties.containsKey(SmtpServer.SMTP_KEY_HOSTNAME)) {
            setHostname((String) properties.remove(SmtpServer.SMTP_KEY_HOSTNAME));
        }
        if (properties.containsKey(SmtpServer.SMTP_KEY_PORT)) {
            setPort(Integer.parseInt((String) properties.remove(SmtpServer.SMTP_KEY_PORT)));
        }
        if (properties.containsKey(SmtpServer.SMTP_KEY_PROTOCOL)) {
            setProtocol((String) properties.remove(SmtpServer.SMTP_KEY_PROTOCOL));
        }
        if (properties.containsKey(SmtpServer.SMTP_KEY_USERNAME)) {
            setUsername((String) properties.remove(SmtpServer.SMTP_KEY_USERNAME));
        }
        if (properties.containsKey(SmtpServer.SMTP_KEY_PASSWORD)) {
            setPassword((String) properties.remove(SmtpServer.SMTP_KEY_PASSWORD));
        }
        if (properties.containsKey(SmtpServer.SMTP_KEY_SMTP_HOSTNAME)) {
            setHostname((String) properties.remove(SmtpServer.SMTP_KEY_SMTP_HOSTNAME));
        }
        if (properties.containsKey(SmtpServer.SMTP_KEY_SMTP_PORT)) {
            setPort(Integer.parseInt((String) properties.remove(SmtpServer.SMTP_KEY_SMTP_PORT)));
        }
        if (properties.containsKey(SmtpServer.SMTP_KEY_SMTP_PROTOCOL)) {
            setProtocol((String) properties.remove(SmtpServer.SMTP_KEY_SMTP_PROTOCOL));
        }
        if (properties.containsKey(SmtpServer.SMTP_KEY_SMTP_USERNAME)) {
            setUsername((String) properties.remove(SmtpServer.SMTP_KEY_SMTP_USERNAME));
        }
        if (properties.containsKey(SmtpServer.SMTP_KEY_SMTP_PASSWORD)) {
            setPassword((String) properties.remove(SmtpServer.SMTP_KEY_SMTP_PASSWORD));
        }
        _mailProperties.clear();
        _mailProperties.putAll(properties);
    }

    @JsonIgnore
    public String getMailProperty(final String property) {
        return _mailProperties.getProperty(property);
    }

    @JsonIgnore
    public String setMailProperty(final String property, final String value) {
        return (String) _mailProperties.setProperty(property, value);
    }

    @JsonIgnore
    public String removeMailProperty(final String property) {
        return (String) _mailProperties.remove(property);
    }

    @JsonIgnore
    public Properties asProperties() {
        final Properties properties = new Properties();
        if (StringUtils.isNotBlank(_hostname)) {
            properties.setProperty(SMTP_KEY_HOSTNAME, _hostname);
        }
        properties.setProperty(SMTP_KEY_PORT, Integer.toString(_port));
        if (StringUtils.isNotBlank(_protocol)) {
            properties.setProperty(SMTP_KEY_PROTOCOL, _protocol);
        }
        if (StringUtils.isNotBlank(_username)) {
            properties.setProperty(SMTP_KEY_USERNAME, _username);
        }
        if (StringUtils.isNotBlank(_password)) {
            properties.setProperty(SMTP_KEY_PASSWORD, _password);
        }
        properties.putAll(_mailProperties);
        return properties;
    }

    private String  _hostname;
    private int     _port;
    private String  _protocol;
    private String  _username;
    private String  _password;
    private final Properties _mailProperties = new Properties();
}
