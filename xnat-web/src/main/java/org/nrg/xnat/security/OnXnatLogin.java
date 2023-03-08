/*
 * web: org.nrg.xnat.security.OnXnatLogin
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.utils.XnatHttpUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class OnXnatLogin extends SavedRequestAwareAuthenticationSuccessHandler {
    private static final String DEFAULT_LANDING         = "/app/template/Index.vm?login=true";

    private final NamedParameterJdbcTemplate _template;

    public OnXnatLogin(final NamedParameterJdbcTemplate template) {
        _template = template;
    }

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) throws IOException, ServletException {
        log.debug("Request is to process authentication");

        XnatHttpUtils.onAuthenticationSuccess(request, _template);

        super.onAuthenticationSuccess(request, response, authentication);
    }

    @Override
    protected String determineTargetUrl(final HttpServletRequest request, final HttpServletResponse response) {
        // TODO: Isn't there an override for the default landing page?
        final String  url           = getDefaultTargetUrl();
        final boolean isRootDefault = "/".equals(url);

        if (isRootDefault) {
            setDefaultTargetUrl(DEFAULT_LANDING);
        }

        // If the app root is the default, go to the default landing, otherwise figure it out.
        return isRootDefault ? DEFAULT_LANDING : super.determineTargetUrl(request, response);
    }
}
