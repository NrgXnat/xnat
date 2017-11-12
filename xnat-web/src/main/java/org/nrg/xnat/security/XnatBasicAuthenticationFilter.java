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
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.nrg.xnat.utils.XnatHttpUtils.getCredentials;

@Slf4j
public class XnatBasicAuthenticationFilter extends BasicAuthenticationFilter {
    @Autowired
    public XnatBasicAuthenticationFilter(final AuthenticationManager manager, final AuthenticationEntryPoint entryPoint) {
        super(manager, entryPoint);
        _authenticationDetailsSource = new WebAuthenticationDetailsSource();
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
            credentials = getCredentials(request);
        } catch (ParseException e) {
            // This means that the basic authentication header was found but wasn't properly formatted, so we can't find credentials.
            throw new ServletException(e.getMessage());
        }

        final String username = credentials.getLeft();
        final String password = credentials.getRight();

        if (StringUtils.isNotBlank(username) && authenticationIsRequired(username)) {
            final UsernamePasswordAuthenticationToken authRequest = _providerManager.buildUPTokenForAuthMethod(_providerManager.retrieveAuthMethod(username), username, password);
            authRequest.setDetails(_authenticationDetailsSource.buildDetails(request));

            try {
                final Authentication authResult = getAuthenticationManager().authenticate(authRequest);
                _authenticationStrategy.onAuthentication(authResult, request, response);

                log.debug("Authentication success: " + authResult.toString());

                SecurityContextHolder.getContext().setAuthentication(authResult);
                onSuccessfulAuthentication(request, response, authResult);
            } catch (AuthenticationException failed) {
                // Authentication failed
                log.info("Authentication request for user: '{}' failed: {}", username, failed.getMessage());

                SecurityContextHolder.getContext().setAuthentication(null);
                onUnsuccessfulAuthentication(request, response, failed);

                XnatAuthenticationFilter.logFailedAttempt(username, request); //originally I put this in the onUnsuccessfulAuthentication method, but that would force me to re-parse the username
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, AdminUtils.GetLoginFailureMessage());
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    // XNAT-2186 requested that REST logins also leave records of last login date
    protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) throws IOException {
        try {
            final UserI user = XDAT.getUserDetails();
            if (user != null) {
                final Object lock = getUserLock(user);

                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (lock) {
                    final Date    today = Calendar.getInstance(java.util.TimeZone.getDefault()).getTime();
                    final XFTItem item  = XFTItem.NewItem("xdat:user_login", user);
                    item.setProperty("xdat:user_login.user_xdat_user_id", user.getID());
                    item.setProperty("xdat:user_login.login_date", today);
                    item.setProperty("xdat:user_login.ip_address", AccessLogger.GetRequestIp(request));
                    item.setProperty("xdat:user_login.session_id", request.getSession().getId());
                    SaveItemHelper.authorizedSave(item, null, true, false, EventUtils.DEFAULT_EVENT(user, null));
                }
                request.getSession().setAttribute("userHelper", UserHelper.getUserHelperService(user));
            }
        } catch (Exception e) {
            log.error("An unknown error occurred", e);
        }

        super.onSuccessfulAuthentication(request, response, authResult);
    }

    private Object getUserLock(final UserI user) {
        final Object lock = LOCKS.get(user.getID());
        if (lock != null) {
            return lock;
        }
        LOCKS.put(user.getID(), new Object());
        return LOCKS.get(user.getID());
    }

    private boolean authenticationIsRequired(String username) {
        // Only re-authenticate if username doesn't match SecurityContextHolder and user isn't authenticated
        // (see SEC-53)
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        if (existingAuth == null || !existingAuth.isAuthenticated()) {
            return true;
        }

        // Limit username comparison to providers which use usernames (ie UsernamePasswordAuthenticationToken)
        // (see SEC-348)

        if (existingAuth instanceof UsernamePasswordAuthenticationToken && !existingAuth.getName().equals(username)) {
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

    private static final Map<Integer, Object> LOCKS = new ConcurrentHashMap<>();

    private final WebAuthenticationDetailsSource _authenticationDetailsSource;
    private       XnatProviderManager            _providerManager;
    private       SessionAuthenticationStrategy  _authenticationStrategy;
}
