/*
 * web: org.nrg.xnat.security.XnatAuthenticationEntryPoint
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xapi.XapiUtils;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.utils.InteractiveAgentDetector;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;

@Slf4j
public class XnatAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {
    public XnatAuthenticationEntryPoint(final String loginFormUrl, final SiteConfigPreferences preferences, final InteractiveAgentDetector detector) {
        super(loginFormUrl);
        _realm = XapiUtils.getWwwAuthenticateBasicHeaderValue(preferences.getSiteId());
        _detector = detector;
    }

    /**
     * Overrides {@link LoginUrlAuthenticationEntryPoint#commence(HttpServletRequest, HttpServletResponse, AuthenticationException)}
     * to test for data path and user agent. If this request is for a data path by an non-interactive agent, the
     * response status is set to HTTP 302, i.e. unauthorized. Otherwise the base implementation is used, which redirects
     * the request to the configured login page.
     *
     * @param request       HTTP request object.
     * @param response      HTTP response object.
     * @param authException An authentication exception that may have redirected the agent to re-authenticate.
     *
     * @throws IOException      When an error occurs reading or writing data.
     * @throws ServletException When an error occurs in the framework.
     */
    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException) throws IOException, ServletException {
        final String strippedUri = request.getRequestURI().substring(request.getContextPath().length());

        log.debug("Evaluating data path request: {}, user agent: {}", strippedUri, request.getHeader("User-Agent"));

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
            response.setHeader(WWW_AUTHENTICATE, _realm);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            super.commence(request, response, authException);
        }
    }

    private final String                   _realm;
    private final InteractiveAgentDetector _detector;
}
