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
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.utils.InteractiveAgentDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class XnatAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {
    public XnatAuthenticationEntryPoint(final String loginFormUrl, final SiteConfigPreferences preferences, final InteractiveAgentDetector detector) {
        super(loginFormUrl);
        _siteId = preferences.getSiteId();
        _detector = detector;
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

        _log.debug("Evaluating data path request: {}, user agent: {}", strippedUri, request.getHeader("User-Agent"));

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

        if (_detector.isDataPath(request) && !_detector.isInteractiveAgent(request)) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"" + _siteId + "\"");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            super.commence(request, response, authException);
        }
    }


    private static final Logger _log = LoggerFactory.getLogger(XnatAuthenticationEntryPoint.class);

    private final String _siteId;
    private final InteractiveAgentDetector _detector;
}
