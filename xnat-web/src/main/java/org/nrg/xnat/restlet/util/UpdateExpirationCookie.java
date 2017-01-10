/*
 * web: org.nrg.xnat.restlet.util.UpdateExpirationCookie
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class UpdateExpirationCookie extends GenericFilterBean {

    public static String name = "SESSION_EXPIRATION_TIME";

    @Override
    public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest  hq              = (HttpServletRequest) req;
        final HttpServletResponse hr              = (HttpServletResponse) resp;
        final int                 sessionIdleTime = hq.getSession().getMaxInactiveInterval();

        final Cookie c = new Cookie(name, "" + (new Date()).getTime() + "," + ((sessionIdleTime * 1000)));
        c.setPath("/");
        hr.addCookie(c);

        chain.doFilter(req, resp);
    }

    @Override
    protected void initFilterBean() throws ServletException {
        _log.debug("Initializing the UpdateExpirationCookie filter bean.");
    }

    private static final Logger _log = LoggerFactory.getLogger(UpdateExpirationCookie.class);
}
