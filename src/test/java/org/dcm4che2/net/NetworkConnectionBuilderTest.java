/*
 * dicomtools: org.dcm4che2.net.NetworkConnectionBuilderTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.dcm4che2.net;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class NetworkConnectionBuilderTest {
    /**
     * Test method for {@link NetworkConnectionBuilder#build()}.
     */
    @Test
    public void testBuild() {
        final NetworkConnection connection = new NetworkConnectionBuilder().build();
        assertNotNull(connection);
    }

    /**
     * Test method for {@link NetworkConnectionBuilder#setHostname(String)}.
     */
    @Test
    public void testSetHostname() {
        final String hostname = "foo.bar.org";
        final NetworkConnection connection = new NetworkConnectionBuilder().setHostname(hostname).build();
        assertEquals(hostname, connection.getHostname());
    }

    /**
     * Test method for {@link NetworkConnectionBuilder#setPort(int)}.
     */
    @Test
    public void testSetPort() {
        final int port = 8023;
        final NetworkConnection c = new NetworkConnectionBuilder().setPort(port).build();
        assertEquals(port, c.getPort());
    }
}
