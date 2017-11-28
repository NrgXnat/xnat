/*
 * web: org.nrg.xnat.security.XnatLogoutSuccessHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class XnatLogoutSuccessHandler extends AbstractAuthenticationTargetUrlRequestHandler implements LogoutSuccessHandler {
    public XnatLogoutSuccessHandler(final boolean requireLogin, final String openXnatLogoutSuccessUrl, final String securedXnatLogoutSuccessUrl) {
        _requireLogin = requireLogin;
        _openXnatLogoutSuccessUrl = openXnatLogoutSuccessUrl;
        _securedXnatLogoutSuccessUrl = securedXnatLogoutSuccessUrl;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        setDefaultTargetUrl(getRequiredLogoutSuccessUrl());
        super.handle(request, response, authentication);
    }

    public void setRequireLogin(final boolean requireLogin) {
        _requireLogin = requireLogin;
    }

    private String getRequiredLogoutSuccessUrl() {
        log.debug("Found require login set to: {}, setting required logout success URL to: {}", _requireLogin, _requireLogin ? _securedXnatLogoutSuccessUrl : _openXnatLogoutSuccessUrl);
        return _requireLogin ? _securedXnatLogoutSuccessUrl : _openXnatLogoutSuccessUrl;
    }

    private final String _openXnatLogoutSuccessUrl;
    private final String _securedXnatLogoutSuccessUrl;

    private boolean _requireLogin;
}
