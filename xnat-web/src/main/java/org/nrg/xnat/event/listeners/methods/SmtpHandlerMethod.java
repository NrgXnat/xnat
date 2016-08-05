package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.mail.services.MailService;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SmtpHandlerMethod extends AbstractNotificationsPreferenceHandlerMethod {
    @Autowired
    public SmtpHandlerMethod(final NotificationsPreferences preferences, final JavaMailSenderImpl mailSender, final MailService mailService) {
        _preferences = preferences;
        this._mailSender = mailSender;
        _mailService = mailService;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateSmtp();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (PREFERENCES.contains(preference)) {
            updateSmtp();
        }
    }

    private void updateSmtp() {
        final Properties oldMailProperties = _mailSender.getJavaMailProperties();

        final boolean smtpEnabled = _preferences.getSmtpEnabled();
        final boolean smtpAuth = _preferences.getSmtpAuth();
        final boolean startTls = _preferences.getSmtpStartTls();
        final String sslTrust = _preferences.getSmtpSSLTrust();

        _mailSender.setHost(_preferences.getHostname());
        _mailSender.setPort(_preferences.getPort());
        _mailSender.setUsername(_preferences.getUsername());
        _mailSender.setPassword(_preferences.getPassword());
        _mailSender.setProtocol(_preferences.getProtocol());
        _mailService.setSmtpEnabled(smtpEnabled);

        oldMailProperties.setProperty("smtp.enabled", String.valueOf(smtpEnabled));
        oldMailProperties.setProperty("mail.smtp.auth", String.valueOf(smtpAuth));
        oldMailProperties.setProperty("mail.smtp.starttls.enable", String.valueOf(startTls));

        if (sslTrust != null) {
            oldMailProperties.setProperty("mail.smtp.ssl.trust", sslTrust);
        }

        _mailSender.setJavaMailProperties(oldMailProperties);
    }

    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("smtp.enabled", "host", "port", "username", "password", "protocol", "smtp.enabled", "mail.smtp.auth", "mail.smtp.starttls.enable", "mail.smtp.ssl.trust"));

    private final NotificationsPreferences _preferences;
    private final JavaMailSenderImpl       _mailSender;
    private final MailService              _mailService;

}
