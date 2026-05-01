/*
 * core: org.nrg.xdat.configuration.TestUsersConfig
 * XNAT https://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.configuration;

import org.nrg.framework.services.ContextService;
import org.nrg.xdat.security.LegacySha256PasswordEncoder;
import org.nrg.xdat.security.validators.PasswordValidator;
import org.nrg.xdat.security.validators.PasswordValidatorChain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class TestUsersConfig {
    @Bean
    public ContextService contextService() {
        return ContextService.getInstance();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        final DelegatingPasswordEncoder encoder = (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
        encoder.setDefaultPasswordEncoderForMatches(new LegacySha256PasswordEncoder());
        return encoder;
    }

    @Bean
    public PasswordValidator validator() {
        return PasswordValidatorChain.getTestInstance();
    }
}
