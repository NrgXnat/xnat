/*
 * org.nrg.notify.configuration.NotificationServiceTestConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.configuration;

import org.nrg.mail.services.MailService;
import org.nrg.notify.entities.ChannelRendererProvider;
import org.nrg.notify.renderers.ChannelRenderer;
import org.nrg.notify.renderers.NrgMailChannelRenderer;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Import(BasicPlatformTestConfiguration.class)
@PropertySource(value = "classpath:org/nrg/notify/services/test.properties")
@ComponentScan("org.nrg.mail.services.impl")
public class NotificationServiceTestConfiguration {
    @Bean
    public JavaMailSenderImpl mailSender(final Environment environment) {
        final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(environment.getProperty("mailserver.host"));
        mailSender.setPort(Integer.parseInt(environment.getProperty("mailserver.port")));
        mailSender.setUsername(environment.getProperty("mailserver.username"));
        mailSender.setPassword(environment.getProperty("mailserver.password"));
        mailSender.setProtocol(environment.getProperty("mailserver.protocol"));
        return mailSender;
    }


    @Bean
    public NrgMailChannelRenderer mailChannelRenderer(final MailService mailService) {
        final NrgMailChannelRenderer renderer = new NrgMailChannelRenderer(mailService);
        renderer.setFromAddress("admin@xnat.org");
        renderer.setSubjectPrefix("Test");
        return renderer;
    }
    @Bean
    public ChannelRendererProvider rendererProvider(final NrgMailChannelRenderer renderer) {
        final ChannelRendererProvider      provider  = new ChannelRendererProvider();
        final Map<String, ChannelRenderer> renderers = new HashMap<>();
        renderers.put("htmlMail", renderer);
        renderers.put("textMail", renderer);
        provider.setRenderers(renderers);
        return provider;
    }
}
