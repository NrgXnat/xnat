package org.nrg.mail.config;

import org.nrg.mail.services.MailService;
import org.nrg.mail.services.impl.MockJavaMailSender;
import org.nrg.mail.services.impl.SpringBasedMailServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class TestSpringBasedMailServiceImplConfig {
    @Bean
    public MockJavaMailSender mailSender() {
        return new MockJavaMailSender();
    }

    @Bean
    public MailService mailService(final JavaMailSender mailSender) {
        return new SpringBasedMailServiceImpl(mailSender);
    }
}
