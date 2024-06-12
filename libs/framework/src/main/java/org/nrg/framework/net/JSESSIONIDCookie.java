/*
 * framework: org.nrg.framework.net.JSESSIONIDCookie
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.net;

import org.apache.commons.lang3.StringUtils;

import java.net.URLConnection;

/**
 * Represent the JSESSIONID cookie that we are manually inserting into REST calls as of Tomcat 7 (the JSESSIONID cookie
 * already present is HttpOnly by default and not visible to JavaScript or other client-side applications).
 *
 * @deprecated This was created to maintain Java 5 compatibility. The Java HttpCookie class or an even higher level of
 * abstraction is preferred.
 */
@Deprecated
public final class JSESSIONIDCookie {
    public JSESSIONIDCookie(final String jsessionid) {
        _jsessionid = jsessionid;
    }

    /**
     * Sets the JSESSIONID cookie in the request headers for the submitted connection.
     *
     * @param connection The connection to be used for authenticated requests.
     */
    public void setInRequestHeader(final URLConnection connection) {
        if (StringUtils.isNotBlank(_jsessionid)) {
            connection.setRequestProperty("Cookie", toString());
        }
    }

    /**
     * Returns a representation of the JSESSIONID value. This is formatted properly for insertion into a cookie request
     * header.
     */
    @Override
    public String toString() {
        return StringUtils.isNotBlank(_jsessionid) ? String.format("JSESSIONID=%s", _jsessionid) : "";
    }

    private final String _jsessionid;
}
