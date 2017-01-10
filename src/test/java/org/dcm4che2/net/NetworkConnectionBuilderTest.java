/*
 * dicomtools: org.dcm4che2.net.NetworkConnectionBuilderTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.dcm4che2.net;

import junit.framework.TestCase;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class NetworkConnectionBuilderTest extends TestCase {

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Test method for {@link org.dcm4che2.net.NetworkConnectionBuilder#NetworkConnectionBuilder()}.
   */
  public final void testNetworkConnectionBuilder() {
    assertTrue(new NetworkConnectionBuilder() instanceof NetworkConnectionBuilder);
  }

  /**
   * Test method for {@link org.dcm4che2.net.NetworkConnectionBuilder#build()}.
   */
  public final void testBuild() {
    final Object c = new NetworkConnectionBuilder().build();
    assertNotNull(c);
    assertTrue(c instanceof NetworkConnection);
  }

  /**
   * Test method for {@link org.dcm4che2.net.NetworkConnectionBuilder#setHostname(java.lang.String)}.
   */
  public final void testSetHostname() {
    final String hostname = "foo.bar.org";
    final NetworkConnection c = new NetworkConnectionBuilder().setHostname(hostname).build();
    assertEquals(hostname, c.getHostname());
  }

  /**
   * Test method for {@link org.dcm4che2.net.NetworkConnectionBuilder#setPort(int)}.
   */
  public final void testSetPort() {
    final int port = 8023;
    final NetworkConnection c = new NetworkConnectionBuilder().setPort(port).build();
    assertEquals(port, c.getPort());
  }
}
