/*
 * web: org.nrg.xnat.security.XnatInitCheckFilter
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.utils.XnatHttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class XnatInitCheckFilter extends GenericFilterBean {
    @Autowired
    public XnatInitCheckFilter(final XnatAppInfo appInfo, final SiteConfigPreferences preferences) {
        super();
        _appInfo = appInfo;
        _preferences = preferences;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest  request  = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        if (_appInfo.isInitialized()) {
            // If the site URL has already been set, do not redirect and save the fact that we're initialized.
            chain.doFilter(req, res);
        } else {
            // We're going to use the user for logging.
            final UserI   user        = XDAT.getUserDetails();
            final boolean isAnonymous = user == null || user.isGuest();

            if (isAnonymous) {
                String header = request.getHeader("Authorization");
                if (header != null && header.startsWith("Basic ") && !_appInfo.isInitPathRequest(request)) {
                    // Users that authenticated using basic authentication receive an error message informing
                    // them that the system is not yet initialized.
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Site has not yet been configured.");
                    return;
                }
            }

            try {
                if (_appInfo.isInitPathRequest(request) ||
                    _appInfo.isConfigPathRequest(request) ||
                    _appInfo.isNonAdminErrorPathRequest(request) ||
                    isPermittedReferer(request)) {
                    //If you're already on the configuration page, error page, or expired password page, continue on without redirect.
                    chain.doFilter(req, res);
                } else if (isAnonymous) {
                    // user not authenticated, let another filter handle the redirect
                    // (NB: I tried putting this check up with the basic auth check,
                    // but you get this weird redirect with 2 login pages on the same screen.  Seems to work here).
                    chain.doFilter(req, res);
                } else {
                    final String serverPath = XnatHttpUtils.getServerRoot(request);
                    if (Roles.isSiteAdmin(user)) {
                        if (_log.isWarnEnabled()) {
                            _log.warn("Admin user {} has logged into the uninitialized server and is being redirected to {}", user.getUsername(), serverPath + _appInfo.getConfigPath());
                        }
                        //Otherwise, if the user has administrative permissions, direct the user to the configuration page.
                        response.sendRedirect(serverPath + _appInfo.getConfigPath());
                    } else {
                        if (_log.isWarnEnabled()) {
                            _log.warn("Non-admin user {} has logged into the uninitialized server and is being redirected to {}", user.getUsername(), serverPath + _appInfo.getNonAdminErrorPath());
                        }
                        //The system is not initialized but the user does not have administrative permissions. Direct the user to an error page.
                        response.sendRedirect(serverPath + _appInfo.getNonAdminErrorPath());
                    }
                }
            } catch (NrgServiceRuntimeException e) {
                if (e.getServiceError().equals(NrgServiceError.SecurityViolation)) {
                    final String referer = request.getHeader("Referer");
                    _log.error("A possible security violation has occurred. An attempt to access {} specifying {} as the referer was made by user {}.", request.getRequestURL().toString(), referer, user == null ? "Unknown" : user.getLogin());
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            }
        }
    }

    private boolean isPermittedReferer(final HttpServletRequest request) {
        final String referer = request.getHeader("Referer");
        if (StringUtils.isBlank(referer)) {
            return false;
        }

        final String uri = request.getRequestURI();
        if (uri.contains("/app/template") || uri.contains("/app/screen") || uri.endsWith(".vm") || uri.equals("/")) {
            return false;
        }

        try {
            final URI refererUri = new URI(referer);

            // This validates the request against the referer to ensure they match (no CSRF).
            final URI requestUri = new URI(request.getRequestURL().toString());
            if ((_preferences.getMatchSecurityProtocol() && !StringUtils.equals(refererUri.getScheme(), requestUri.getScheme())) ||
                !StringUtils.equals(refererUri.getHost(), requestUri.getHost()) ||
                refererUri.getPort() != requestUri.getPort()) {
                final String message = String.format("The referer and request URIs were different:\n * Request: scheme %s, host %s, port %d\n * Referer: scheme %s, host %s, port %d",
                        requestUri.getScheme(), requestUri.getHost(), requestUri.getPort(), refererUri.getScheme(), refererUri.getHost(), refererUri.getPort());
                throw new NrgServiceRuntimeException(NrgServiceError.SecurityViolation, message);
            }

            // If you're on a request within the configuration page (or error page or expired password page), continue
            // on without redirect.
            final String path = refererUri.getPath();
            return _appInfo.isConfigPathRequest(path) || _appInfo.isNonAdminErrorPathRequest(path) || _appInfo.isOpenUrlRequest(path);
        } catch (URISyntaxException e) {
            _log.warn("Unable to construct a URI from the referer specified: {}", referer);
            return false;
        }
    }

    private static Logger _log = LoggerFactory.getLogger(XnatInitCheckFilter.class);

    private final XnatAppInfo           _appInfo;
    private final SiteConfigPreferences _preferences;
}
