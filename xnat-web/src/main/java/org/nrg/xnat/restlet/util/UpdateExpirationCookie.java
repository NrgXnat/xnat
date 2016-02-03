/*
 * org.nrg.xnat.restlet.util.UpdateExpirationCookie
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.util;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class UpdateExpirationCookie implements Filter {
	public static String name = "SESSION_EXPIRATION_TIME";
	

	@Override
	public void destroy() {}
	@Override
	public void doFilter(ServletRequest req, 
						   ServletResponse resp,
						   FilterChain chain) throws IOException, ServletException {
		HttpServletRequest hq = (HttpServletRequest) req;
		HttpServletResponse hr = (HttpServletResponse) resp;
		int sessionIdleTime = hq.getSession().getMaxInactiveInterval();

		Cookie c=new Cookie(name, ""+(new Date()).getTime()+","+((sessionIdleTime *1000))); 
		c.setPath("/");
		hr.addCookie(c);
		
		chain.doFilter(req,resp);
	}
	
	@Override
	public void init(FilterConfig fg) throws ServletException {}
}
