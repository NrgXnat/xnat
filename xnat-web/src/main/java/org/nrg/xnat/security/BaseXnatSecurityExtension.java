package org.nrg.xnat.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Provides default implementations of the methods defined in {@link XnatSecurityExtension}. This allows
 * implementing classes to override a single method and not have to provide a no-op implementation of the
 * other method.
 */
@Slf4j
public abstract class BaseXnatSecurityExtension implements XnatSecurityExtension {
    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final HttpSecurity http) throws Exception {
        log.debug("Performing default no-op on HTTP security configuration");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final AuthenticationManagerBuilder builder) throws Exception {
        log.debug("Performing default no-op on authentication manager configuration");
    }
}
