package org.nrg.xnat.configuration;

import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.notify.entities.ChannelRendererProvider;
import org.nrg.notify.renderers.ChannelRenderer;
import org.nrg.notify.renderers.NrgMailChannelRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan({"org.nrg.mail.services", "org.nrg.notify.services.impl", "org.nrg.notify.daos"})
public class NotificationsConfig {

    @Bean
    public JavaMailSender mailSender() {
        final JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(_host);
        sender.setPort(_port);
        sender.setUsername(_username);
        sender.setPassword(_password);
        return sender;
    }

    @Bean
    public HibernateEntityPackageList nrgNotificationEntityPackages() {
        return new HibernateEntityPackageList(Collections.singletonList("org.nrg.notify.entities"));
    }

    @Bean
    public ChannelRenderer mailChannelRenderer() {
        final NrgMailChannelRenderer renderer = new NrgMailChannelRenderer();
        renderer.setFromAddress(_fromAddress);
        renderer.setSubjectPrefix(_subjectPrefix);
        return renderer;
    }

    @Bean
    public Map<String, ChannelRenderer> renderers() {
        final Map<String, ChannelRenderer> renderers = new HashMap<>();
        renderers.put("htmlMail", mailChannelRenderer());
        renderers.put("textMail", mailChannelRenderer());
        return renderers;
    }

    @Bean
    public ChannelRendererProvider rendererProvider() {
        final ChannelRendererProvider provider = new ChannelRendererProvider();
        provider.setRenderers(renderers());
        return provider;
    }

    @Value("${mailserver.host}")
    private String _host;
    
    @Value("${mailserver.port}")
    private int _port;

    @Value("${mailserver.username}")
    private String _username;

    @Value("${mailserver.password}")
    private String _password;

    @Value("${mailserver.admin}")
    private String _fromAddress;

    @Value("${mailserver.prefix}")
    private String _subjectPrefix;
}
