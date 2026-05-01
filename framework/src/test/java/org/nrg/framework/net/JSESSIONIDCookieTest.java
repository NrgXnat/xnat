/*
 * framework: org.nrg.framework.net.JSESSIONIDCookieTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.net;

import java.net.URL;
import java.net.URLConnection;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
@SuppressWarnings("deprecation")
public class JSESSIONIDCookieTest {

    /**
     * Test method for {@link JSESSIONIDCookie#setInRequestHeader(URLConnection)}.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testSetInRequestHeader() throws Exception {
        final URLConnection conn = new URL("https://nrg.wustl.edu").openConnection();
        assertThat(conn.getRequestProperty("Cookie")).isNull();
        final JSESSIONIDCookie cookie = new JSESSIONIDCookie("abcdef");
        cookie.setInRequestHeader(conn);
        assertThat(conn.getRequestProperty("Cookie")).isNotNull()
                                                     .isNotEmpty()
                                                     .isEqualTo("JSESSIONID=abcdef");
    }

    /**
     * Test method for {@link JSESSIONIDCookie#setInRequestHeader(URLConnection)}.
     */
    @Test
    public void testNullSetInRequestHeader() throws Exception {
        final URLConnection conn = new URL("https://nrg.wustl.edu").openConnection();
        assertThat(conn.getRequestProperty("Cookie")).isNull();
        final JSESSIONIDCookie cookie = new JSESSIONIDCookie(null);
        cookie.setInRequestHeader(conn);
        assertThat(conn.getRequestProperty("Cookie")).isNull();
    }

    /**
     * Test method for {@link JSESSIONIDCookie#setInRequestHeader(URLConnection)}.
     */
    @Test
    public void testEmptySetInRequestHeader() throws Exception {
        final URLConnection conn = new URL("https://nrg.wustl.edu").openConnection();
        assertThat(conn.getRequestProperty("Cookie")).isNull();
        final JSESSIONIDCookie cookie = new JSESSIONIDCookie("");
        cookie.setInRequestHeader(conn);
        assertThat(conn.getRequestProperty("Cookie")).isNull();
    }

    /**
     * Test method for {@link JSESSIONIDCookie#toString()}.
     */
    @Test
    public void testToString() {
        final JSESSIONIDCookie cookie = new JSESSIONIDCookie("abcdef");
        assertThat(cookie).isNotNull()
                          .asString()
                          .isNotEmpty()
                          .isEqualTo("JSESSIONID=abcdef");
        final JSESSIONIDCookie nullCookie = new JSESSIONIDCookie(null);
        assertThat(nullCookie).isNotNull()
                              .asString()
                              .isEmpty();
        final JSESSIONIDCookie emptyCookie = new JSESSIONIDCookie("");
        assertThat(emptyCookie).isNotNull()
                               .asString()
                               .isEmpty();
    }
}
