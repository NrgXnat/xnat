/*
 * web: org.nrg.xnat.configuration.NotificationsConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.mail.services.EmailRequestLogService;
import org.nrg.mail.services.MailService;
import org.nrg.notify.entities.ChannelRendererProvider;
import org.nrg.notify.renderers.ChannelRenderer;
import org.nrg.notify.renderers.NrgMailChannelRenderer;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.preferences.SmtpServer;
import org.nrg.xnat.security.ResetEmailRequests;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan({"org.nrg.mail.services", "org.nrg.notify.services.impl", "org.nrg.notify.daos"})
public class NotificationsConfig {
    @Bean
    public TriggerTask resetEmailRequests(final EmailRequestLogService service) {
        return new TriggerTask(new ResetEmailRequests(service), new PeriodicTrigger(900000));
    }

    @Bean
    public JavaMailSenderImpl mailSender(final NotificationsPreferences preferences) {
        final SmtpServer         smtp   = preferences.getSmtpServer();
        final JavaMailSenderImpl sender = new JavaMailSenderImpl();
        if(smtp!=null) {
            sender.setHost(StringUtils.defaultIfBlank(smtp.getHostname(), "localhost"));
            sender.setPort(smtp.getPort());
            sender.setUsername(StringUtils.defaultIfBlank(smtp.getUsername(), ""));
            sender.setPassword(StringUtils.defaultIfBlank(smtp.getPassword(), ""));
            sender.setProtocol(StringUtils.defaultIfBlank(smtp.getProtocol(), "smtp"));
            if (smtp.getMailProperties().size() > 0) {
                sender.setJavaMailProperties(smtp.getMailProperties());
            }
        }
        return sender;
    }

    @Bean
    public HibernateEntityPackageList nrgNotificationEntityPackages() {
        return new HibernateEntityPackageList(Collections.singletonList("org.nrg.notify.entities"));
    }

    @Bean
    public NrgMailChannelRenderer mailChannelRenderer(final SiteConfigPreferences siteConfigPreferences, final NotificationsPreferences notificationsPreferences, final MailService mailService) {
        final NrgMailChannelRenderer renderer = new NrgMailChannelRenderer(mailService);
        renderer.setFromAddress(siteConfigPreferences.getAdminEmail());
        renderer.setSubjectPrefix(notificationsPreferences.getEmailPrefix());
        return renderer;
    }

    @Bean
    public ChannelRendererProvider rendererProvider(final NrgMailChannelRenderer renderer) {
        final ChannelRendererProvider provider = new ChannelRendererProvider();
        final Map<String, ChannelRenderer> renderers = new HashMap<>();
        renderers.put("htmlMail", renderer);
        renderers.put("textMail", renderer);
        provider.setRenderers(renderers);
        return provider;
    }
}
