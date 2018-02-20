/*
 * web: org.nrg.xnat.restlet.util.UpdateExpirationCookie
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@Slf4j
public class UpdateExpirationCookie extends GenericFilterBean {
    public static String name = "SESSION_EXPIRATION_TIME";

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest  request         = (HttpServletRequest) servletRequest;
        final HttpServletResponse response        = (HttpServletResponse) servletResponse;
        final int                 sessionIdleTime = request.getSession().getMaxInactiveInterval();

        final Cookie cookie = new Cookie(name, new Date().getTime() + "," + sessionIdleTime * 1000);
        cookie.setPath(request.getContextPath() + "/");
        response.addCookie(cookie);
        log.debug("Updated cookie {} to value {}.", cookie.getName(), cookie.getValue());

        chain.doFilter(request, response);
    }

    @Override
    protected void initFilterBean() {
        log.debug("Initializing the UpdateExpirationCookie filter bean.");
    }
}
