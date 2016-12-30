package org.nrg.xdat.configuration;

import org.nrg.xdat.security.validators.PasswordValidatorChain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestUsersConfig {
    @Bean
    public PasswordValidatorChain validator() {
        return PasswordValidatorChain.getTestInstance();
    }
}
