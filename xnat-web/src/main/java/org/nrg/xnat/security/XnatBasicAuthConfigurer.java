/*
 * web: org.nrg.xnat.security.XnatBasicAuthConfigurer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.*;
import org.springframework.stereotype.Component;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Used in place of the Spring Security HttpBasicConfigurer to allow inserting the {@link XnatBasicAuthenticationFilter}
 * in place of the default Spring basic authentication filter.
 *
 * @author Rick Herrick &lt;jrherrick@wustl.edu&gt;
 * @since 1.7.5
 */
@SuppressWarnings("UnusedReturnValue")
@Slf4j
@Component
public final class XnatBasicAuthConfigurer<B extends HttpSecurityBuilder<B>> extends AbstractHttpConfigurer<XnatBasicAuthConfigurer<B>, B> {
    private static final MediaType[] MEDIA_TYPES = {MediaType.APPLICATION_ATOM_XML,
                                                    MediaType.APPLICATION_FORM_URLENCODED,
                                                    MediaType.APPLICATION_JSON,
                                                    MediaType.APPLICATION_OCTET_STREAM,
                                                    MediaType.APPLICATION_XML,
                                                    MediaType.MULTIPART_FORM_DATA,
                                                    MediaType.TEXT_XML};

    /**
     * Creates a new instance
     *
     * @throws Exception When an error occurs during initialization.
     */
    public XnatBasicAuthConfigurer(final AuthenticationEntryPoint entryPoint) throws Exception {
        realmName(DEFAULT_REALM);

        final LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> entryPoints = new LinkedHashMap<>();
        entryPoints.put(X_REQUESTED_WITH, new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

        final DelegatingAuthenticationEntryPoint defaultEntryPoint = new DelegatingAuthenticationEntryPoint(entryPoints);
        defaultEntryPoint.setDefaultEntryPoint(entryPoint);
        _authenticationEntryPoint = defaultEntryPoint;
    }

    /**
     * Allows easily changing the realm, but leaving the remaining defaults in place. If
     * {@link #authenticationEntryPoint(AuthenticationEntryPoint)} has been invoked,
     * invoking this method will result in an error.
     *
     * @param realmName the HTTP Basic realm to use
     *
     * @return {@link XnatBasicAuthConfigurer} for additional customization
     *
     * @throws Exception When an error occurs setting the realm name.
     */
    public XnatBasicAuthConfigurer<B> realmName(final String realmName) throws Exception {
        _basicAuthenticationEntryPoint.setRealmName(realmName);
        _basicAuthenticationEntryPoint.afterPropertiesSet();
        return this;
    }

    /**
     * The {@link AuthenticationEntryPoint} to be populated on
     * {@link BasicAuthenticationFilter} in the event that authentication fails. The
     * default to use {@link BasicAuthenticationEntryPoint} with the realm
     * "Spring Security Application".
     *
     * @param authenticationEntryPoint the {@link AuthenticationEntryPoint} to use
     *
     * @return {@link XnatBasicAuthConfigurer} for additional customization
     */
    @SuppressWarnings("unused")
    public XnatBasicAuthConfigurer<B> authenticationEntryPoint(final AuthenticationEntryPoint authenticationEntryPoint) {
        _authenticationEntryPoint = authenticationEntryPoint;
        return this;
    }

    /**
     * Specifies a custom {@link AuthenticationDetailsSource} to use for basic
     * authentication. The default is {@link WebAuthenticationDetailsSource}.
     *
     * @param authenticationDetailsSource the custom {@link AuthenticationDetailsSource} to use
     *
     * @return {@link XnatBasicAuthConfigurer} for additional customization
     */
    @SuppressWarnings("unused")
    public XnatBasicAuthConfigurer<B> authenticationDetailsSource(final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        _authenticationDetailsSource = authenticationDetailsSource;
        return this;
    }

    @Override
    public void init(final B http) {
        final ContentNegotiationStrategy contentNegotiationStrategy = getContentNegotiatingStrategy(http);

        final MediaTypeRequestMatcher restMatcher = new MediaTypeRequestMatcher(contentNegotiationStrategy, MEDIA_TYPES);
        restMatcher.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));

        final RequestMatcher notHtmlMatcher = new NegatedRequestMatcher(new MediaTypeRequestMatcher(contentNegotiationStrategy, MediaType.TEXT_HTML));
        final RequestMatcher restNotHtmlMatcher = new AndRequestMatcher(Arrays.asList(notHtmlMatcher, restMatcher));
        final RequestMatcher preferredMatcher = new OrRequestMatcher(Arrays.asList(X_REQUESTED_WITH, restNotHtmlMatcher));

        registerDefaultEntryPoint(http, preferredMatcher);
        registerDefaultLogoutSuccessHandler(http, preferredMatcher);
    }

    private ContentNegotiationStrategy getContentNegotiatingStrategy(final B http) {
        final ContentNegotiationStrategy strategy = http.getSharedObject(ContentNegotiationStrategy.class);
        return strategy != null ? strategy : new HeaderContentNegotiationStrategy();
    }

    private void registerDefaultEntryPoint(final B http, final RequestMatcher preferredMatcher) {
        //noinspection unchecked
        final ExceptionHandlingConfigurer<B> exceptionHandling = http.getConfigurer(ExceptionHandlingConfigurer.class);
        if (exceptionHandling == null) {
            return;
        }
        exceptionHandling.defaultAuthenticationEntryPointFor(postProcess(_authenticationEntryPoint), preferredMatcher);
    }

    private void registerDefaultLogoutSuccessHandler(final B http, final RequestMatcher preferredMatcher) {
        //noinspection unchecked
        final LogoutConfigurer<B> logout = http.getConfigurer(LogoutConfigurer.class);
        if (logout == null) {
            return;
        }
        logout.defaultLogoutSuccessHandlerFor(postProcess(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)), preferredMatcher);
    }

    @Override
    public void configure(final B http) {
        final AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        final XnatBasicAuthenticationFilter basicAuthenticationFilter = new XnatBasicAuthenticationFilter(authenticationManager, _authenticationEntryPoint);
        if (_authenticationDetailsSource != null) {
            basicAuthenticationFilter.setAuthenticationDetailsSource(_authenticationDetailsSource);
        }
        final RememberMeServices rememberMeServices = http.getSharedObject(RememberMeServices.class);
        if (rememberMeServices != null) {
            basicAuthenticationFilter.setRememberMeServices(rememberMeServices);
        }
        http.addFilter(postProcess(basicAuthenticationFilter));
    }

    private static final RequestHeaderRequestMatcher X_REQUESTED_WITH = new RequestHeaderRequestMatcher("X-Requested-With", "XMLHttpRequest");
    private static final String                      DEFAULT_REALM    = "XNAT";

    private AuthenticationEntryPoint                           _authenticationEntryPoint;
    private AuthenticationDetailsSource<HttpServletRequest, ?> _authenticationDetailsSource;

    private BasicAuthenticationEntryPoint _basicAuthenticationEntryPoint = new BasicAuthenticationEntryPoint();
}