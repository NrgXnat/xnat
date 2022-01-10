/*
 * web: org.nrg.xnat.security.XnatUrlAuthenticationFailureHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.security.exceptions.NewAutoAccountNotAutoEnabledException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Getter
@Accessors(prefix = "_")
@Slf4j
public class XnatUrlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    public XnatUrlAuthenticationFailureHandler(final String defaultFailureUrl, final String defaultDisabledAccountUrl, final String newLdapAccountNotAutoEnabledFailureUrl) {
        super(defaultFailureUrl);
        _defaultDisabledAccountUrl              = defaultDisabledAccountUrl;
        _newLdapAccountNotAutoEnabledFailureUrl = newLdapAccountNotAutoEnabledFailureUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException exception) throws IOException, ServletException {
        if (exception instanceof NewAutoAccountNotAutoEnabledException) {
            onAuthenticationFailureCustomHandler(request, response, getNewLdapAccountNotAutoEnabledFailureUrl(), exception);
        } else if (exception instanceof DisabledException) {
            onAuthenticationFailureCustomHandler(request, response, getDefaultDisabledAccountUrl(), exception);
        } else {
            super.onAuthenticationFailure(request, response, exception);
        }
    }

    private void onAuthenticationFailureCustomHandler(final HttpServletRequest request, final HttpServletResponse response, final String handlerUrl, final AuthenticationException exception) throws IOException, ServletException {
        saveException(request, exception);
        if (isUseForward()) {
            log.debug("Forwarding to {}", handlerUrl);
            request.getRequestDispatcher(handlerUrl).forward(request, response);
        } else {
            log.debug("Redirecting to {}", handlerUrl);
            getRedirectStrategy().sendRedirect(request, response, handlerUrl);
        }
    }

    private final String _defaultDisabledAccountUrl;
    private final String _newLdapAccountNotAutoEnabledFailureUrl;
}
