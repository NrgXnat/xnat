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
     * Called while {@link SecurityConfig#securityFilterChain} builds the filter chain (formerly the
     * adapter's {@code configure(HttpSecurity)}). This allows the implementing class to add further
     * configurations to the HTTP security configuration.
     *
     * @param http The HTTP security configuration.
     */
    void configure(final HttpSecurity http) throws Exception;

    /**
     * Called while {@link SecurityConfig#authenticationManager} builds the authentication manager
     * (formerly the adapter's {@code configure(AuthenticationManagerBuilder)}). This allows the
     * implementing class to add further configurations to the authentication manager configuration.
     *
     * @param builder The authentication manager configuration.
     */
    void configure(final AuthenticationManagerBuilder builder) throws Exception;
}
