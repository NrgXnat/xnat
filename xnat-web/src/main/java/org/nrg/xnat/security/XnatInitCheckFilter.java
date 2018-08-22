/*
 * web: org.nrg.xnat.security.XnatInitCheckFilter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.utils.XnatHttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class XnatInitCheckFilter extends OncePerRequestFilter {
    @Autowired
    public XnatInitCheckFilter(final XnatAppInfo appInfo) {
        super();
        _appInfo = appInfo;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws ServletException, IOException {
        if (_appInfo.isInitialized()) {
            // If the site URL has already been set, do not redirect and save the fact that we're initialized.
            chain.doFilter(request, response);
        } else {
            // We're going to use the user for logging.
            final UserI   user        = XDAT.getUserDetails();
            final boolean isAnonymous = user == null || user.isGuest();

            if (isAnonymous) {
                final String header = request.getHeader("Authorization");
                if (header != null && header.startsWith("Basic ") && !_appInfo.isInitPathRequest(request)) {
                    // Users that authenticated using basic authentication receive an error message informing
                    // them that the system is not yet initialized.
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Site has not yet been configured.");
                    return;
                }
            }

            try {
                if (_appInfo.isInitPathRequest(request) ||
                    _appInfo.isSetupPathRequest(request) ||
                    _appInfo.isNonAdminErrorPathRequest(request)) {
                    //If you're already on the configuration page, error page, or expired password page, continue on without redirect.
                    chain.doFilter(request, response);
                } else if (isAnonymous) {
                    // user not authenticated, let another filter handle the redirect
                    // (NB: I tried putting this check up with the basic auth check,
                    // but you get this weird redirect with 2 login pages on the same screen.  Seems to work here).
                    chain.doFilter(request, response);
                } else {
                    final String serverPath = XnatHttpUtils.getServerRoot(request);
                    if (Roles.isSiteAdmin(user)) {
                        if (log.isWarnEnabled()) {
                            log.warn("Admin user {} has logged into the uninitialized server and is being redirected to {}", user.getUsername(), serverPath + _appInfo.getSetupPath());
                        }
                        //Otherwise, if the user has administrative permissions, direct the user to the configuration page.
                        response.sendRedirect(serverPath + _appInfo.getSetupPath());
                    } else {
                        if (log.isWarnEnabled()) {
                            log.warn("Non-admin user {} has logged into the uninitialized server and is being redirected to {}", user.getUsername(), serverPath + _appInfo.getNonAdminErrorPath());
                        }
                        //The system is not initialized but the user does not have administrative permissions. Direct the user to an error page.
                        response.sendRedirect(serverPath + _appInfo.getNonAdminErrorPath());
                    }
                }
            } catch (NrgServiceRuntimeException e) {
                if (e.getServiceError().equals(NrgServiceError.SecurityViolation)) {
                    final String referer = request.getHeader("Referer");
                    log.error("A possible security violation has occurred. An attempt to access {} specifying {} as the referer was made by user {}.", request.getRequestURL().toString(), referer, user == null ? "Unknown" : user.getLogin());
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            }
        }
    }

    private final XnatAppInfo _appInfo;
}
