/*
 * DicomUtils: org.dcm4che2.net.NetworkApplicationEntityBuilderTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.dcm4che2.net;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import junit.framework.TestCase;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class NetworkApplicationEntityBuilderTest extends TestCase {

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
   * Test method for {@link org.dcm4che2.net.NetworkApplicationEntityBuilder#NetworkApplicationEntityBuilder()}.
   */
  public final void testNetworkApplicationEntityBuilder() {
    assertTrue(new NetworkApplicationEntityBuilder() instanceof NetworkApplicationEntityBuilder);
  }

  /**
   * Test method for {@link org.dcm4che2.net.NetworkApplicationEntityBuilder#build()}.
   */
  public final void testBuild() {
    final Object entity = new NetworkApplicationEntityBuilder().build();
    assertNotNull(entity);
    assertTrue(entity instanceof NetworkApplicationEntity);
  }

  /**
   * Test method for {@link org.dcm4che2.net.NetworkApplicationEntityBuilder#setAETitle(java.lang.String)}.
   */
  public final void testSetAETitle() {
    final String aeTitle = "foo";
    final NetworkApplicationEntity entity = new NetworkApplicationEntityBuilder().setAETitle(aeTitle).build();
    assertEquals(aeTitle, entity.getAETitle());
  }

  /**
   * Test method for {@link org.dcm4che2.net.NetworkApplicationEntityBuilder#setNetworkConnection(org.dcm4che2.net.NetworkConnection)}.
   */
  public final void testSetNetworkConnection() {
    final NetworkConnection nc = new NetworkConnection();
    final NetworkApplicationEntity entity = new NetworkApplicationEntityBuilder().setNetworkConnection(nc).build();
    assertTrue(nc == entity.getNetworkConnection()[0]);

    // TODO: multiple NCs?
  }

  /**
   * Test method for {@link org.dcm4che2.net.NetworkApplicationEntityBuilder#setAssociationInitiator()}.
   */
  public final void testSetAssociationInitiator() {
    final NetworkApplicationEntity entity = new NetworkApplicationEntityBuilder().setAssociationInitiator().build();
    assertTrue(entity.isAssociationInitiator());
  }

  /**
   * Test method for {@link org.dcm4che2.net.NetworkApplicationEntityBuilder#setAssociationAcceptor()}.
   */
  public final void testSetAssociationAcceptor() {
    final NetworkApplicationEntity entity = new NetworkApplicationEntityBuilder().setAssociationAcceptor().build();
    assertTrue(entity.isAssociationAcceptor());
  }

  /**
   * Test method for {@link org.dcm4che2.net.NetworkApplicationEntityBuilder#setTransferCapability(org.dcm4che2.net.TransferCapability[])}.
   */
  public final void testSetTransferCapability() {
    final TransferCapability a = new TransferCapability();
    final TransferCapability b = new TransferCapability();
    final TransferCapability c = new TransferCapability();
    final TransferCapability d = new TransferCapability();

    final NetworkApplicationEntity entity = new NetworkApplicationEntityBuilder().setTransferCapability(a, b, c).build();

    final Collection<TransferCapability> tcs = new HashSet<TransferCapability>(Arrays.asList(entity.getTransferCapability()));
    assertTrue(tcs.contains(a));
    assertTrue(tcs.contains(b));
    assertTrue(tcs.contains(c));
    assertFalse(tcs.contains(d));
  }
}
