/*
 * web: org.nrg.xnat.security.XnatAuthenticationEntryPoint
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class XnatAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    public XnatAuthenticationEntryPoint(final String loginFormUrl, final SiteConfigPreferences preferences) {
        super(loginFormUrl);
        _preferences = preferences;
    }

    /**
     * Overrides {@link LoginUrlAuthenticationEntryPoint#commence(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.security.core.AuthenticationException)}
     * to test for data path and user agent. If this request is for a data path by an non-interactive agent, the
     * response status is set to HTTP 302, i.e. unauthorized. Otherwise the base implementation is used, which redirects
     * the request to the configured login page.
     *
     * @param request       HTTP request object.
     * @param response      HTTP response object.
     * @param authException An authentication exception that may have redirected the agent to re-authenticate.
     *
     * @throws IOException When an error occurs reading or writing data.
     * @throws ServletException When an error occurs in the framework.
     */
    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException) throws IOException, ServletException {
        final String strippedUri = request.getRequestURI().substring(request.getContextPath().length());
        final String userAgent = request.getHeader("User-Agent");

        if (_log.isDebugEnabled()) {
            _log.debug("Evaluating data path request: " + strippedUri + ", user agent: " + userAgent);
        }

        if (!StringUtils.isBlank(strippedUri) && strippedUri.contains("/action/AcceptProjectAccess/par/")) {
            int index = strippedUri.indexOf("/par/") + 5;
            if (strippedUri.length() > index) {//par number included?
                String parS = strippedUri.substring(index);
                if (parS.contains("/")) {
                    parS = parS.substring(0, parS.indexOf("/"));
                }

                request.getSession().setAttribute("par", parS);
            }
        }

        if (isDataPath(request) && !isInteractiveAgent(userAgent)) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"" + _preferences.getSiteId() + "\"");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            super.commence(request, response, authException);
        }
    }

    /**
     * Sets the data paths, i.e. those paths which require a user-agent interactivity test to determine whether the user
     * should be denied as unauthorized or redirected to the login page. Each data path should be a valid Ant-style
     * pattern matching the URL(s) to be designated as data paths.
     *
     * @param dataPaths A list of strings in Ant-style patterns indicating data paths.
     */
    public void setDataPaths(final List<String> dataPaths) {
        if (_log.isDebugEnabled()) {
            _log.debug("Adding " + dataPaths + " data paths");
        }

        for (final String dataPath : dataPaths) {
            if (_log.isDebugEnabled()) {
                _log.debug("Adding data path: " + dataPath);
            }
            _dataPaths.add(new AntPathRequestMatcher(dataPath));
        }
    }

    /**
     * Sets the list of interactive agents to redirect to the login page even on data paths.
     *
     * @param interactiveAgents A list of interactive agents to be directed to the login page even on data paths.
     */
    public void setInteractiveAgents(final List<String> interactiveAgents) {
        for (final String interactiveAgent : interactiveAgents) {
            if (_log.isDebugEnabled()) {
                _log.debug("Adding interactive agent specifier: " + interactiveAgent);
            }
            final Pattern pattern = Pattern.compile(interactiveAgent);
            _agentPatterns.add(pattern);
        }
    }

    private boolean isDataPath(final HttpServletRequest request) {
        if (_log.isDebugEnabled()) {
            _log.debug("Testing URI as data path: " + request.getContextPath());
        }
        for (final RequestMatcher dataPath : _dataPaths) {
            if (dataPath.matches(request)) {
                if (_log.isDebugEnabled()) {
                    _log.debug("URI " + request.getContextPath() + "is a data path.");
                }
                return true;
            }
        }
        if (_log.isDebugEnabled()) {
            _log.debug("URI " + request.getContextPath() + "is not a data path.");
        }
        return false;
    }

    private boolean isInteractiveAgent(final String userAgent) {
        if (_log.isDebugEnabled()) {
            _log.debug("Testing user agent as interactive: " + userAgent);
        }
        if (!StringUtils.isBlank(userAgent)) {
            for (Pattern interactiveAgent : _agentPatterns) {
                if (interactiveAgent.matcher(userAgent).matches()) {
                    if (_log.isDebugEnabled()) {
                        _log.debug("User agent " + userAgent + " is interactive, matched simple regex pattern: " + interactiveAgent);
                    }
                    return true;
                }
            }
        }
        if (_log.isDebugEnabled()) {
            _log.debug("User agent " + userAgent + " is not interactive, failed to match any patterns");
        }
        return false;
    }

    private final SiteConfigPreferences _preferences;

    private static final Log _log = LogFactory.getLog(XnatAuthenticationEntryPoint.class);

    private final List<RequestMatcher> _dataPaths     = new ArrayList<>();
    private final List<Pattern>        _agentPatterns = new ArrayList<>();
}
