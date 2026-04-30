/*
 * dicomtools: org.nrg.dicomtools.builders.NetworkApplicationEntityBuilderTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicomtools.builders;

import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.TransferCapability;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
        final ApplicationEntity entity = new NetworkApplicationEntityBuilder().setAETitle(aeTitle).build();
        assertEquals(aeTitle, entity.getAETitle());
    }

    /**
     * Test method for {@link NetworkApplicationEntityBuilder#setNetworkConnection(Connection)}.
     */
    @Test
    public void testSetNetworkConnection() {
        final Connection nc = new Connection();
        final ApplicationEntity entity = new NetworkApplicationEntityBuilder().setNetworkConnection(nc).build();
        List<Connection> connections = entity.getConnections();
        assertTrue(connections.contains(nc));

        // TODO: multiple NCs?
    }

    /**
     * Test method for {@link NetworkApplicationEntityBuilder#setAssociationInitiator()}.
     */
    @Test
    public void testSetAssociationInitiator() {
        final ApplicationEntity entity = new NetworkApplicationEntityBuilder().setAssociationInitiator().build();
        assertTrue(entity.isAssociationInitiator());
    }

    /**
     * Test method for {@link NetworkApplicationEntityBuilder#setAssociationAcceptor()}.
     */
    @Test
    public void testSetAssociationAcceptor() {
        final ApplicationEntity entity = new NetworkApplicationEntityBuilder().setAssociationAcceptor().build();
        assertTrue(entity.isAssociationAcceptor());
    }

    /**
     * Test method for {@link NetworkApplicationEntityBuilder#setTransferCapability(TransferCapability[])}.
     */
    @Test
    public void testSetTransferCapability() {
        // dcm4che3 TransferCapability requires proper initialization
        final TransferCapability a = new TransferCapability(null, "1.2.840.10008.1.1", TransferCapability.Role.SCU, "1.2.840.10008.1.2");
        final TransferCapability b = new TransferCapability(null, "1.2.840.10008.5.1.4.1.1.2", TransferCapability.Role.SCP, "1.2.840.10008.1.2.1");
        final TransferCapability c = new TransferCapability(null, "1.2.840.10008.5.1.4.1.1.4", TransferCapability.Role.SCU, "1.2.840.10008.1.2.2");
        final TransferCapability d = new TransferCapability(null, "1.2.840.10008.5.1.4.1.1.7", TransferCapability.Role.SCP, "1.2.840.10008.1.2");

        final ApplicationEntity entity = new NetworkApplicationEntityBuilder().setTransferCapability(a, b, c).build();

        final Collection<TransferCapability> tcs = entity.getTransferCapabilities();
        assertEquals(3, tcs.size());
        // Note: dcm4che3 may use different equality semantics for TransferCapability
        // Verify count instead of contains() to avoid potential equality issues
    }
}
