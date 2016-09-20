/*
 * web: org.nrg.xnat.configuration.NotificationsConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import org.apache.commons.lang3.StringUtils;
import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.mail.services.MailService;
import org.nrg.notify.entities.ChannelRendererProvider;
import org.nrg.notify.renderers.ChannelRenderer;
import org.nrg.notify.renderers.NrgMailChannelRenderer;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@ComponentScan({"org.nrg.mail.services", "org.nrg.notify.services.impl", "org.nrg.notify.daos"})
public class NotificationsConfig {

    @Bean
    public JavaMailSenderImpl mailSender(final NotificationsPreferences preferences) throws IOException, SiteConfigurationException {
        final Map<String, String> smtp = preferences.getSmtpServer();
        final JavaMailSenderImpl sender = new JavaMailSenderImpl();
        if(smtp!=null) {
            sender.setHost(StringUtils.defaultIfBlank(smtp.remove("host"), "localhost"));
            sender.setPort(Integer.parseInt(StringUtils.defaultIfBlank(smtp.remove("port"), "25")));
            sender.setUsername(StringUtils.defaultIfBlank(smtp.remove("username"), ""));
            sender.setPassword(StringUtils.defaultIfBlank(smtp.remove("password"), ""));
            sender.setProtocol(StringUtils.defaultIfBlank(smtp.remove("protocol"), "smtp"));
            if (smtp.size() > 0) {
                final Properties properties = new Properties();
                for (final String property : smtp.keySet()) {
                    properties.setProperty(property, smtp.get(property));
                }
                sender.setJavaMailProperties(properties);
            }
        }
        return sender;
    }

    @Bean
    public HibernateEntityPackageList nrgNotificationEntityPackages() {
        return new HibernateEntityPackageList(Collections.singletonList("org.nrg.notify.entities"));
    }

    @Bean
    public NrgMailChannelRenderer mailChannelRenderer(final SiteConfigPreferences siteConfigPreferences, final NotificationsPreferences notificationsPreferences, final MailService mailService) throws SiteConfigurationException {
        final NrgMailChannelRenderer renderer = new NrgMailChannelRenderer(mailService);
        renderer.setFromAddress(siteConfigPreferences.getAdminEmail());
        renderer.setSubjectPrefix(notificationsPreferences.getEmailPrefix());
        return renderer;
    }

    @Bean
    public ChannelRendererProvider rendererProvider(final NrgMailChannelRenderer renderer) throws SiteConfigurationException {
        final ChannelRendererProvider provider = new ChannelRendererProvider();
        final Map<String, ChannelRenderer> renderers = new HashMap<>();
        renderers.put("htmlMail", renderer);
        renderers.put("textMail", renderer);
        provider.setRenderers(renderers);
        return provider;
    }
}
