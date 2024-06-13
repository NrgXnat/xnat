/*
 * dicomtools: org.dcm4che2.net.NetworkApplicationEntityBuilderTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.dcm4che2.net;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class NetworkApplicationEntityBuilderTest {
    /**
     * Test method for {@link NetworkApplicationEntityBuilder#build()}.
     */
    @Test
    public void testBuild() {
        final Object entity = new NetworkApplicationEntityBuilder().build();
        assertNotNull(entity);
    }

    /**
     * Test method for {@link NetworkApplicationEntityBuilder#setAETitle(String)}.
     */
    @Test
    public void testSetAETitle() {
        final String aeTitle = "foo";
        final NetworkApplicationEntity entity = new NetworkApplicationEntityBuilder().setAETitle(aeTitle).build();
        assertEquals(aeTitle, entity.getAETitle());
    }

    /**
     * Test method for {@link NetworkApplicationEntityBuilder#setNetworkConnection(NetworkConnection)}.
     */
    @Test
    public void testSetNetworkConnection() {
        final NetworkConnection nc = new NetworkConnection();
        final NetworkApplicationEntity entity = new NetworkApplicationEntityBuilder().setNetworkConnection(nc).build();
        assertTrue(nc == entity.getNetworkConnection()[0]);

        // TODO: multiple NCs?
    }

    /**
     * Test method for {@link NetworkApplicationEntityBuilder#setAssociationInitiator()}.
     */
    @Test
    public void testSetAssociationInitiator() {
        final NetworkApplicationEntity entity = new NetworkApplicationEntityBuilder().setAssociationInitiator().build();
        assertTrue(entity.isAssociationInitiator());
    }

    /**
     * Test method for {@link NetworkApplicationEntityBuilder#setAssociationAcceptor()}.
     */
    @Test
    public void testSetAssociationAcceptor() {
        final NetworkApplicationEntity entity = new NetworkApplicationEntityBuilder().setAssociationAcceptor().build();
        assertTrue(entity.isAssociationAcceptor());
    }

    /**
     * Test method for {@link NetworkApplicationEntityBuilder#setTransferCapability(TransferCapability[])}.
     */
    @Test
    public void testSetTransferCapability() {
        final TransferCapability a = new TransferCapability();
        final TransferCapability b = new TransferCapability();
        final TransferCapability c = new TransferCapability();
        final TransferCapability d = new TransferCapability();

        final NetworkApplicationEntity entity = new NetworkApplicationEntityBuilder().setTransferCapability(a, b, c).build();

        final Collection<TransferCapability> tcs = new HashSet<>(Arrays.asList(entity.getTransferCapability()));
        assertTrue(tcs.contains(a));
        assertTrue(tcs.contains(b));
        assertTrue(tcs.contains(c));
        assertFalse(tcs.contains(d));
    }
}
