/*
 * core: org.nrg.xdat.configuration.TestUsersConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.configuration;

import org.nrg.framework.services.ContextService;
import org.nrg.xdat.security.validators.PasswordValidatorChain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestUsersConfig {
    @Bean
    public ContextService contextService() {
        return ContextService.getInstance();
    }

    @Bean
    public PasswordValidatorChain validator() {
        return PasswordValidatorChain.getTestInstance();
    }
}
