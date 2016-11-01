/*
 * web: org.nrg.xnat.event.listeners.methods.SmtpHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.SmtpServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SmtpHandlerMethod extends AbstractNotificationsPreferenceHandlerMethod {
    @Autowired
    public SmtpHandlerMethod(final NotificationsPreferences preferences, final JavaMailSenderImpl mailSender) {
        _preferences = preferences;
        _mailSender = mailSender;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        updateSmtp();
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        updateSmtp();
    }

    private void updateSmtp() {
        final SmtpServer smtpServer = _preferences.getSmtpServer();

        _mailSender.setHost(smtpServer.getHostname());
        _mailSender.setPort(smtpServer.getPort());
        _mailSender.setUsername(smtpServer.getUsername());
        _mailSender.setPassword(smtpServer.getPassword());
        _mailSender.setProtocol(smtpServer.getProtocol());

        if (!smtpServer.getSmtpAuth()) {
            _mailSender.setJavaMailProperties(SMTP_AUTH_DISABLED);
        } else {
            final Properties properties = new Properties();
            properties.setProperty(SmtpServer.SMTP_KEY_AUTH, "true");
            if (smtpServer.getSmtpStartTls()) {
                properties.setProperty(SmtpServer.SMTP_KEY_STARTTLS_ENABLE, "true");
            }
            if (StringUtils.isNotBlank(smtpServer.getSmtpSslTrust())) {
                properties.setProperty(SmtpServer.SMTP_KEY_SSL_TRUST, smtpServer.getSmtpSslTrust());
            }
            properties.putAll(smtpServer.getMailProperties());
            _mailSender.setJavaMailProperties(properties);
        }
    }

    private static final List<String> PREFERENCES        = ImmutableList.copyOf(Arrays.asList("smtpEnabled", "smtpHostname", "smtpPort", "smtpUsername", "smtpPassword", "smtpProtocol", "smtpAuth", "smtpStartTls", "smtpSslTrust"));
    private static final Properties   SMTP_AUTH_DISABLED = new Properties() {{
        setProperty(SmtpServer.SMTP_KEY_AUTH, "false");
    }};

    private final NotificationsPreferences _preferences;
    private final JavaMailSenderImpl       _mailSender;
}
