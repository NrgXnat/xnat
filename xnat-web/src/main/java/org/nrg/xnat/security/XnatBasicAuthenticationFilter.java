/*
 * web: org.nrg.xnat.security.XnatBasicAuthenticationFilter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;

import static org.nrg.xnat.utils.XnatHttpUtils.getBasicAuthCredentials;

@Component
@Slf4j
public class XnatBasicAuthenticationFilter extends BasicAuthenticationFilter {
    @Autowired
    public XnatBasicAuthenticationFilter(final AuthenticationManager manager, final AuthenticationEntryPoint entryPoint, final AliasTokenService aliasTokenService) {
        super(manager, entryPoint);
        _authenticationDetailsSource = new WebAuthenticationDetailsSource();
        _aliasTokenService = aliasTokenService;
    }

    @Autowired
    public void setXnatProviderManager(final XnatProviderManager providerManager) {
        _providerManager = providerManager;
    }

    @Autowired
    public void setSessionAuthenticationStrategy(final SessionAuthenticationStrategy strategy) {
        _authenticationStrategy = strategy;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final Pair<String, String> credentials;
        try {
            credentials = getBasicAuthCredentials(request);
        } catch (ParseException e) {
            // This means that the basic authentication header was found but wasn't properly formatted, so we can't find credentials.
            throw new ServletException(e.getMessage());
        }

        if (credentials != null) {
            String username = credentials.getLeft();
            String providerName = null;
            int idx = username.indexOf('/');
            if (idx != -1) {
                providerName = username.substring(0, idx);
                username = username.substring(idx + 1);
            }
            final String password = credentials.getRight();

            if (StringUtils.isNotBlank(username) && authenticationIsRequired(username)) {
                final UsernamePasswordAuthenticationToken authRequest;
                if (StringUtils.isBlank(providerName)) {
                    authRequest = _providerManager.buildUPTokenForAuthMethod(_providerManager.retrieveAuthMethod(username),
                            username, password);
                } else {
                    authRequest = _providerManager.buildUPTokenForProviderName(providerName, username, password);
                }
                authRequest.setDetails(_authenticationDetailsSource.buildDetails(request));

                try {
                    final Authentication authResult = getAuthenticationManager().authenticate(authRequest);
                    _authenticationStrategy.onAuthentication(authResult, request, response);

                    AccessLogger.LogServiceAccess(username, request, "Authentication", "SUCCESS");
                    log.debug("Authentication success, got principal of type {}", authResult.getClass().getName());

                    SecurityContextHolder.getContext().setAuthentication(authResult);
                    onSuccessfulAuthentication(request, response, authResult);
                } catch (AuthenticationException failed) {
                    // Authentication failed
                    log.info("Authentication request for user: '{}' failed: {}", username, failed.getMessage());

                    SecurityContextHolder.getContext().setAuthentication(null);
                    onUnsuccessfulAuthentication(request, response, failed);

                    XnatAuthenticationFilter.logFailedAttempt(username, request, failed); //originally I put this in the onUnsuccessfulAuthentication method, but that would force me to re-parse the username
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, AdminUtils.GetLoginFailureMessage());
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    // XNAT-2186 requested that REST logins also leave records of last login date
    protected void onSuccessfulAuthentication(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) throws IOException {
        try {
            Users.recordUserLogin(request);
        } catch (Exception e) {
            log.error("An unknown error occurred", e);
        }

        super.onSuccessfulAuthentication(request, response, authentication);
    }

    private boolean authenticationIsRequired(final String username) {
        // Only re-authenticate if username doesn't match SecurityContextHolder and user isn't authenticated
        // (see SEC-53)
        final Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        if (existingAuth == null || !existingAuth.isAuthenticated()) {
            return true;
        }

        // Limit username comparison to providers which use usernames (ie UsernamePasswordAuthenticationToken)
        // (see SEC-348)

        final String authName = existingAuth.getName();
        if (existingAuth instanceof UsernamePasswordAuthenticationToken && StringUtils.isNotBlank(authName) && !StringUtils.equalsAny(authName, username, getUsernameForToken(username))) {
            return true;
        }

        // Handle unusual condition where an AnonymousAuthenticationToken is already present
        // This shouldn't happen very often, as BasicProcessingFilter is meant to be earlier in the filter
        // chain than AnonymousAuthenticationFilter. Nevertheless, presence of both an AnonymousAuthenticationToken
        // together with a BASIC authentication request header should indicate re-authentication using the
        // BASIC protocol is desirable. This behaviour is also consistent with that provided by form and digest,
        // both of which force re-authentication if the respective header is detected (and in doing so replace
        // any existing AnonymousAuthenticationToken). See SEC-610.
        return existingAuth instanceof AnonymousAuthenticationToken;
    }

    private String getUsernameForToken(final String username) {
        final AliasToken token = _aliasTokenService.locateToken(username);
        return token == null ? null : token.getXdatUserId();
    }

    private final WebAuthenticationDetailsSource _authenticationDetailsSource;
    private final AliasTokenService              _aliasTokenService;
    private       XnatProviderManager            _providerManager;
    private       SessionAuthenticationStrategy  _authenticationStrategy;
}
