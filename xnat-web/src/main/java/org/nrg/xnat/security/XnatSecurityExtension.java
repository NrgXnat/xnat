package org.nrg.xnat.security;

import org.nrg.xnat.initialization.SecurityConfig;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Defines the interface for classes that extend and customize the XNAT {@link SecurityConfig security configuration}.
 * The defined methods are called from the corresponding methods in the security configuration.
 */
public interface XnatSecurityExtension {
    /**
     * Indicates the authentication method for the security extension.
     *
     * @return The authentication method.
     */
    String getAuthMethod();

    /**
     * Called by the {@link SecurityConfig#securityFilterChain(HttpSecurity, org.springframework.security.authentication.AuthenticationManager)} method. This allows the implementing class to add
     * further configurations to the HTTP security configuration.
     *
     * @param http The HTTP security configuration.
     */
    void configure(final HttpSecurity http) throws Exception;

    /**
     * Called by the {@link SecurityConfig#authenticationManager(org.springframework.security.config.annotation.ObjectPostProcessor)} method. This allows the implementing
     * class to add further configurations to the authentication manager configuration.
     *
     * @param builder The authentication manager configuration.
     */
    void configure(final AuthenticationManagerBuilder builder) throws Exception;
}
