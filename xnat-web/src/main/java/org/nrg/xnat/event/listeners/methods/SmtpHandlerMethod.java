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
public class SmtpHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    @Autowired
    public SmtpHandlerMethod(final SpringBasedMailServiceImpl mailService) {
        super(NotificationsPreferences.class, SMTP_ENABLED, SMTP_HOSTNAME, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD, SMTP_PROTOCOL, SMTP_AUTH, SMTP_START_TLS, SMTP_SSL_TRUST);
        _mailService = mailService;
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
    protected void handlePreferenceImpl(final String preference, final String value) {
        final JavaMailSenderImpl mailSender = (JavaMailSenderImpl) _mailService.getJavaMailSender();
        final Properties         properties = mailSender.getJavaMailProperties();

        switch (preference) {
            case SMTP_ENABLED:
                _mailService.setSmtpEnabled(Boolean.parseBoolean(value));
                break;

            case SMTP_HOSTNAME:
                mailSender.setHost(value);
                break;

            case SMTP_PORT:
                mailSender.setPort(Integer.parseInt(value));
                break;

            case SMTP_USERNAME:
                mailSender.setUsername(value);
                break;

            case SMTP_PASSWORD:
                mailSender.setPassword(value);
                break;

            case SMTP_PROTOCOL:
                mailSender.setProtocol(value);
                break;

            case SMTP_AUTH:
                final boolean isSmtpAuthEnabled = Boolean.parseBoolean(value);
                if (isSmtpAuthEnabled) {
                    properties.setProperty(SmtpServer.SMTP_KEY_AUTH, "true");
                } else {
                    properties.remove(SmtpServer.SMTP_KEY_AUTH);
                    properties.remove(SmtpServer.SMTP_KEY_STARTTLS_ENABLE);
                    properties.remove(SmtpServer.SMTP_KEY_SSL_TRUST);
                }
                mailSender.setJavaMailProperties(properties);
                break;

            case SMTP_START_TLS:
                properties.setProperty(SmtpServer.SMTP_KEY_STARTTLS_ENABLE, value);
                mailSender.setJavaMailProperties(properties);
                break;

            case SMTP_SSL_TRUST:
                if (StringUtils.isNotBlank(value)) {
                    properties.setProperty(SmtpServer.SMTP_KEY_SSL_TRUST, value);
                }
                mailSender.setJavaMailProperties(properties);
                break;

            default:
                if (StringUtils.isBlank(value) && properties.containsKey(value)) {
                    properties.remove(value);
                } else {
                    properties.setProperty(preference, value);
                }
        }
    }

    private static final List<Pattern> PREFS_PATTERNS = ImmutableList.copyOf(Collections.singletonList(Pattern.compile("^smtp[A-Z].*$")));
    private static final String        SMTP_ENABLED   = "smtpEnabled";
    private static final String        SMTP_HOSTNAME  = "smtpHostname";
    private static final String        SMTP_PORT      = "smtpPort";
    private static final String        SMTP_USERNAME  = "smtpUsername";
    private static final String        SMTP_PASSWORD  = "smtpPassword";
    private static final String        SMTP_PROTOCOL  = "smtpProtocol";
    private static final String        SMTP_AUTH      = "smtpAuth";
    private static final String        SMTP_START_TLS = "smtpStartTls";
    private static final String        SMTP_SSL_TRUST = "smtpSslTrust";

    private final SpringBasedMailServiceImpl _mailService;
}
