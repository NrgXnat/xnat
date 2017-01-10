/*
 * web: org.nrg.xnat.utils.XnatHttpUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class XnatHttpUtils {
	
	public static String getJSESSIONID(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase("jsessionid")) {
                	return cookie.getValue();
                }
            }
        }
        throw new JSESSIONIDCookieNotFoundException();
	}

	public static String getJSESSIONID(RunData runData) {
		return getJSESSIONID(runData.getRequest());
	}

    public static String getServerRoot(final HttpServletRequest request) {
        final String port = request.getServerPort() == 80 ? "" : ":" + request.getServerPort();
        final String servletPath = StringUtils.defaultIfBlank(request.getContextPath(), "");
        return String.format("%s://%s%s%s", request.getScheme(), request.getServerName(), port, servletPath);
    }
}
