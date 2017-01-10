/*
 * web: org.nrg.xnat.event.listeners.methods.SmtpHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.nrg.mail.services.impl.SpringBasedMailServiceImpl;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.SmtpServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class SmtpHandlerMethod extends AbstractNotificationsPreferenceHandlerMethod {
    @Autowired
    public SmtpHandlerMethod(final NotificationsPreferences preferences, final SpringBasedMailServiceImpl mailService) {
        _preferences = preferences;
        _mailService = mailService;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    /**
     * This implementation overrides the default version of this method to perform regular expression comparisons
     * against the submitted preference values.
     */
    @Override
    public Set<String> findHandledPreferences(final Collection<String> preferences) {
        final Set<String> handled = Sets.newHashSet();
        for (final Pattern pattern : PREFS_PATTERNS) {
            for (final String preference : preferences) {
                if (pattern.matcher(preference).matches()) {
                    handled.add(preference);
                }
            }
        }
        return handled;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        updateSmtp(values);
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        updateSmtp(Collections.singletonMap(preference, value));
    }

    private void updateSmtp(final Map<String, String> values) {
        _mailService.setSmtpEnabled(_preferences.getSmtpEnabled());

        final JavaMailSenderImpl mailSender = (JavaMailSenderImpl) _mailService.getJavaMailSender();
        final SmtpServer         smtpServer = _preferences.getSmtpServer();

        mailSender.setHost(smtpServer.getHostname());
        mailSender.setPort(smtpServer.getPort());
        mailSender.setUsername(smtpServer.getUsername());
        mailSender.setPassword(smtpServer.getPassword());
        mailSender.setProtocol(smtpServer.getProtocol());

        final Properties properties = mailSender.getJavaMailProperties();

        if (!smtpServer.getSmtpAuth()) {
            properties.remove(SmtpServer.SMTP_KEY_AUTH);
            properties.remove(SmtpServer.SMTP_KEY_STARTTLS_ENABLE);
            properties.remove(SmtpServer.SMTP_KEY_SSL_TRUST);
        } else {
            properties.setProperty(SmtpServer.SMTP_KEY_AUTH, "true");
            properties.setProperty(SmtpServer.SMTP_KEY_STARTTLS_ENABLE, Boolean.toString(smtpServer.getSmtpStartTls()));
            if (StringUtils.isNotBlank(smtpServer.getSmtpSslTrust())) {
                properties.setProperty(SmtpServer.SMTP_KEY_SSL_TRUST, smtpServer.getSmtpSslTrust());
            }
            properties.putAll(smtpServer.getMailProperties());
        }

        for (final String property : values.keySet()) {
            if (!PREFERENCES.contains(property)) {
                final String value = values.get(property);
                if (StringUtils.isBlank(value) && properties.containsKey(value)) {
                    properties.remove(value);
                } else {
                    properties.setProperty(property, value);
                }
            }
        }

        mailSender.setJavaMailProperties(properties);
    }

    private static final List<String>  PREFERENCES    = ImmutableList.copyOf(Arrays.asList("smtpHostname", "smtpPort", "smtpUsername", "smtpPassword", "smtpProtocol", "smtpAuth", "smtpStartTls", "smtpSslTrust"));
    private static final List<Pattern> PREFS_PATTERNS = ImmutableList.copyOf(Collections.singletonList(Pattern.compile("^smtp[A-Z].*$")));

    private final NotificationsPreferences   _preferences;
    private final SpringBasedMailServiceImpl _mailService;
}
