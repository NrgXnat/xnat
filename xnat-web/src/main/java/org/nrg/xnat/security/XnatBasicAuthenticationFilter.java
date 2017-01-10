/*
 * web: org.nrg.xnat.security.XnatBasicAuthenticationFilter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import com.google.common.collect.Maps;
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
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

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

    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final boolean debug = logger.isDebugEnabled();

        String header = request.getHeader("Authorization");

        if ((header != null) && header.startsWith("Basic ")) {
            byte[] base64Token = header.substring(6).getBytes("UTF-8");
            String token       = new String(Base64.decode(base64Token), getCredentialsCharset(request));

            String username = "";
            String password = "";
            int    position = token.indexOf(":");

            if (position != -1) {
                username = token.substring(0, position);
                password = token.substring(position + 1);
            }

            if (debug) {
                logger.debug("Basic Authentication Authorization header found for user '" + username + "'");
            }

            if (authenticationIsRequired(username)) {
                UsernamePasswordAuthenticationToken authRequest = _providerManager.buildUPTokenForAuthMethod(_providerManager.retrieveAuthMethod(username), username, password);
                authRequest.setDetails(_authenticationDetailsSource.buildDetails(request));

                final Authentication authResult;

                try {
                    authResult = getAuthenticationManager().authenticate(authRequest);
                    _authenticationStrategy.onAuthentication(authResult, request, response);
                } catch (AuthenticationException failed) {
                    // Authentication failed
                    if (debug) {
                        logger.debug("Authentication request for user: " + username + " failed: " + failed.toString());
                    }

                    SecurityContextHolder.getContext().setAuthentication(null);
                    onUnsuccessfulAuthentication(request, response, failed);

                    XnatAuthenticationFilter.logFailedAttempt(username, request);//originally I put this in the onUnsuccessfulAuthentication method, but that would force me to re-parse the username
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, AdminUtils.GetLoginFailureMessage());

                    return;
                }

                // Authentication success
                if (debug) {
                    logger.debug("Authentication success: " + authResult.toString());
                }

                SecurityContextHolder.getContext().setAuthentication(authResult);
                onSuccessfulAuthentication(request, response, authResult);
            }
        }

        chain.doFilter(request, response);
    }

    private static final Map<Integer, Object> locks = Maps.newConcurrentMap();

    @Override
    // XNAT-2186 requested that REST logins also leave records of last login date
    protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              Authentication authResult) throws IOException {
        try {
            final UserI user = XDAT.getUserDetails();
            if (user != null) {
                Object lock = locks.get(user.getID());
                if (lock == null) {
                    locks.put(user.getID(), new Object());
                    lock = locks.get(user.getID());
                }

                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (lock) {
                    Date    today = Calendar.getInstance(java.util.TimeZone.getDefault()).getTime();
                    XFTItem item  = XFTItem.NewItem("xdat:user_login", user);
                    item.setProperty("xdat:user_login.user_xdat_user_id", user.getID());
                    item.setProperty("xdat:user_login.login_date", today);
                    item.setProperty("xdat:user_login.ip_address", AccessLogger.GetRequestIp(request));
                    item.setProperty("xdat:user_login.session_id", request.getSession().getId());
                    SaveItemHelper.authorizedSave(item, null, true, false, EventUtils.DEFAULT_EVENT(user, null));
                }
                request.getSession().setAttribute("userHelper", UserHelper.getUserHelperService(user));
            }
        } catch (Exception e) {
            logger.error(e);
        }

        super.onSuccessfulAuthentication(request, response, authResult);
    }

    private final WebAuthenticationDetailsSource _authenticationDetailsSource;
    private       XnatProviderManager            _providerManager;
    private       SessionAuthenticationStrategy  _authenticationStrategy;
}
