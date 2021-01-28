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
    public XnatUrlAuthenticationFailureHandler(final String defaultFailureUrl, final String newLdapAccountNotAutoEnabledFailureUrl) {
        super(defaultFailureUrl);
        _newLdapAccountNotAutoEnabledFailureUrl = newLdapAccountNotAutoEnabledFailureUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException exception) throws IOException, ServletException {
        if (exception instanceof NewAutoAccountNotAutoEnabledException) {
            onAuthenticationFailureNewLdapAccountNotAutoEnabled(request, response, exception);
        } else {
            super.onAuthenticationFailure(request, response, exception);
        }
    }

    private void onAuthenticationFailureNewLdapAccountNotAutoEnabled(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException exception) throws IOException, ServletException {
        saveException(request, exception);

        if (isUseForward()) {
            log.debug("Forwarding to {}", getNewLdapAccountNotAutoEnabledFailureUrl());
            request.getRequestDispatcher(getNewLdapAccountNotAutoEnabledFailureUrl()).forward(request, response);
        } else {
            log.debug("Redirecting to {}", getNewLdapAccountNotAutoEnabledFailureUrl());
            getRedirectStrategy().sendRedirect(request, response, getNewLdapAccountNotAutoEnabledFailureUrl());
        }
    }

    private final String _newLdapAccountNotAutoEnabledFailureUrl;
}
