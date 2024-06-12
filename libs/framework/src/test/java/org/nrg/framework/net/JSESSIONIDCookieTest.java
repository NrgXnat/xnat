/*
 * framework: org.nrg.framework.net.JSESSIONIDCookieTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.net;

import static org.junit.Assert.*;

import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class JSESSIONIDCookieTest {

    /**
     * Test method for {@link org.nrg.net.JSESSIONIDCookie#setInRequestHeader(java.net.URLConnection)}.
     */
    @Test
    public void testSetInRequestHeader() throws Exception {
        final URLConnection conn = new URL("http://nrg.wustl.edu").openConnection();
        assertNull(conn.getRequestProperty("Cookie"));
        final JSESSIONIDCookie cookie = new JSESSIONIDCookie("abcdef");
        cookie.setInRequestHeader(conn);
        assertEquals("JSESSIONID=abcdef", conn.getRequestProperty("Cookie"));
    }

    /**
     * Test method for {@link org.nrg.net.JSESSIONIDCookie#setInRequestHeader(java.net.URLConnection)}.
     */
    @Test
    public void testNullSetInRequestHeader() throws Exception {
        final URLConnection conn = new URL("http://nrg.wustl.edu").openConnection();
        assertNull(conn.getRequestProperty("Cookie"));
        final JSESSIONIDCookie cookie = new JSESSIONIDCookie(null);
        cookie.setInRequestHeader(conn);
        assertNull(conn.getRequestProperty("Cookie"));
    }

    /**
     * Test method for {@link org.nrg.net.JSESSIONIDCookie#setInRequestHeader(java.net.URLConnection)}.
     */
    @Test
    public void testEmptySetInRequestHeader() throws Exception {
        final URLConnection conn = new URL("http://nrg.wustl.edu").openConnection();
        assertNull(conn.getRequestProperty("Cookie"));
        final JSESSIONIDCookie cookie = new JSESSIONIDCookie("");
        cookie.setInRequestHeader(conn);
        assertNull(conn.getRequestProperty("Cookie"));
    }

    /**
     * Test method for {@link org.nrg.net.JSESSIONIDCookie#toString()}.
     */
    @Test
    public void testToString() {
        final JSESSIONIDCookie c = new JSESSIONIDCookie("abcdef");
        assertEquals("JSESSIONID=abcdef", c.toString());
        final JSESSIONIDCookie nullc = new JSESSIONIDCookie(null);
        assertEquals("", nullc.toString());
        final JSESSIONIDCookie emptyc = new JSESSIONIDCookie("");
        assertEquals("", emptyc.toString());
    }
}
