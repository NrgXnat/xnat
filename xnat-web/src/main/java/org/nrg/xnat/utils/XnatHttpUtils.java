/*
 * org.nrg.xnat.utils.XnatHttpUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.utils;

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
}
