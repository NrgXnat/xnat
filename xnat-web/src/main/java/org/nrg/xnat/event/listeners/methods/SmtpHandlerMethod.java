package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.mail.services.MailService;
import org.nrg.xdat.XDAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.*;

@Component
public class SmtpHandlerMethod extends AbstractNotificationsPreferenceHandlerMethod {
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
        if(PREFERENCES.contains(preference)){
            updateSmtp();
        }
    }

    private void updateSmtp(){
		try {
            mailSender.setHost(XDAT.getNotificationsPreferences().getHostname());
            mailSender.setPort(XDAT.getNotificationsPreferences().getPort());
            mailSender.setUsername(XDAT.getNotificationsPreferences().getUsername());
            mailSender.setPassword(XDAT.getNotificationsPreferences().getPassword());
            mailSender.setProtocol(XDAT.getNotificationsPreferences().getProtocol());

            Properties oldMailProperties = mailSender.getJavaMailProperties();
            boolean smtpEnabled = XDAT.getNotificationsPreferences().getSmtpEnabled();
            boolean smtpAuth = XDAT.getNotificationsPreferences().getSmtpAuth();
            boolean startTls = XDAT.getNotificationsPreferences().getSmtpStartTls();
            String sslTrust = XDAT.getNotificationsPreferences().getSmtpSSLTrust();
            _mailService.setSmtpEnabled(smtpEnabled);
            oldMailProperties.setProperty("smtp.enabled",String.valueOf(smtpEnabled));
            oldMailProperties.setProperty("mail.smtp.auth",String.valueOf(smtpAuth));
            oldMailProperties.setProperty("mail.smtp.starttls.enable",String.valueOf(startTls));
            if(sslTrust!=null) {
                oldMailProperties.setProperty("mail.smtp.ssl.trust", sslTrust);
            }
            mailSender.setJavaMailProperties(oldMailProperties);

		} catch (Exception e1) {
			_log.error("", e1);
		}
	}

    private static final Logger       _log        = LoggerFactory.getLogger(SmtpHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("smtp.enabled", "host","port", "username","password", "protocol","smtp.enabled", "mail.smtp.auth","mail.smtp.starttls.enable", "mail.smtp.ssl.trust"));

    @Inject
    private JavaMailSenderImpl mailSender;

    @Autowired
    private MailService _mailService;

}
