/*
 * org.nrg.xnat.security.XnatInitCheckFilter
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.security;

import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.utils.XnatHttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.GenericFilterBean;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class XnatInitCheckFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest  request  = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        if (_appInfo.isInitialized()) {
            // If the site URL has already been set, do not redirect and save the fact that we're initialized.
            chain.doFilter(req, res);
        } else {
            // We're going to use the user for logging.
            final UserI  user = XDAT.getUserDetails();

            final String uri  = request.getRequestURI();

            if (user == null) {
                String header = request.getHeader("Authorization");
                if (header != null && header.startsWith("Basic ") && !isInitializerPath(uri)) {
                    // Users that authenticated using basic authentication receive an error message informing
                    // them that the system is not yet initialized.
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Site has not yet been configured.");
                    return;
                }
            }

            final String referer = request.getHeader("Referer");

            if (isInitializerPath(uri) ||
                _configurationPathPattern.matcher(uri).matches() ||
                _nonAdminErrorPathPattern.matcher(uri).matches() ||
                isExemptedPath(uri)) {
                //If you're already on the configuration page, error page, or expired password page, continue on without redirect.
                chain.doFilter(req, res);
            } else if (referer != null && (_configurationPathPattern.matcher(referer).matches() || _nonAdminErrorPathPattern.matcher(referer).matches() || isExemptedPath(referer)) && !uri.contains("/app/template") && !uri.contains("/app/screen") && !uri.endsWith(".vm") && !uri.equals("/")) {
                //If you're on a request within the configuration page (or error page or expired password page), continue on without redirect. This checks that the referer is the configuration page and that
                // the request is not for another page (preventing the user from navigating away from the Configuration page via the menu bar).
                chain.doFilter(req, res);
            } else {
                if (user == null) {
                    // user not authenticated, let another filter handle the redirect
                    // (NB: I tried putting this check up with the basic auth check,
                    // but you get this weird redirect with 2 login pages on the same screen.  Seems to work here).
                    chain.doFilter(req, res);
                } else {
                    final String serverPath = XnatHttpUtils.getServerRoot(request);
                    if (Roles.isSiteAdmin(user)) {
                        if (_log.isWarnEnabled()) {
                            _log.warn("Admin user {} has logged into the uninitialized server and is being redirected to {}", user.getUsername(), serverPath + _configurationPath);
                        }
                        //Otherwise, if the user has administrative permissions, direct the user to the configuration page.
                        response.sendRedirect(serverPath + _configurationPath);
                    } else {
                        if (_log.isWarnEnabled()) {
                            _log.warn("Non-admin user {} has logged into the uninitialized server and is being redirected to {}", user.getUsername(), serverPath + _nonAdminErrorPath);
                        }
                        //The system is not initialized but the user does not have administrative permissions. Direct the user to an error page.
                        response.sendRedirect(serverPath + _nonAdminErrorPath);
                    }
                }
            }
        }
    }

    public void setInitializationPaths(final List<String> initializationPaths) {
        for (final String initializationPath : initializationPaths) {
            _initializationPathPatterns.add(Pattern.compile("^(https*://.*)?" + initializationPath + ".*$"));
        }
    }

    public void setConfigurationPath(String configurationPath) {
        _configurationPath = configurationPath;
        _configurationPathPattern = Pattern.compile("^(https*://.*)?" + configurationPath + "/*");
    }

    public void setNonAdminErrorPath(String nonAdminErrorPath) {
        _nonAdminErrorPath = nonAdminErrorPath;
        _nonAdminErrorPathPattern = Pattern.compile("^(https*://.*)?" + nonAdminErrorPath + "/*");
    }

    public void setExemptedPaths(List<String> exemptedPaths) {
        _exemptedPaths.clear();
        _exemptedPaths.addAll(exemptedPaths);
    }

    private boolean isExemptedPath(final String path) {
        for (final String exemptedPath : _exemptedPaths) {
            if (path.split("\\?")[0].endsWith(exemptedPath)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInitializerPath(final String uri) {
        for (final Pattern initializationPathPattern : _initializationPathPatterns) {
            if (initializationPathPattern.matcher(uri).matches()) {
                return true;
            }
        }
        return false;
    }

    private static Logger _log = LoggerFactory.getLogger(XnatInitCheckFilter.class);

    @Inject
    private XnatAppInfo _appInfo;

    private String  _configurationPath;
    private String  _nonAdminErrorPath;
    private Pattern _configurationPathPattern;
    private Pattern _nonAdminErrorPathPattern;

    private List<Pattern> _initializationPathPatterns = new ArrayList<>();

    private final List<String> _exemptedPaths = new ArrayList<>();
}
