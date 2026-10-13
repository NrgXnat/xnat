package org.nrg.dcm.scp;

import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.junit.Test;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.DicomObjectIdentifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A C-STORE association reads its receiver's settings and builds its identifier once, with its first instance.
 */
public class ReceiverSettingsTest {
    private static final String AE_TITLE = "XNAT";
    private static final int    PORT     = 8104;

    @Test
    public void anAssociationReadsTheReceiverOnce() {
        final DicomSCPManager manager = managerFor(receiver());
        final Association association = association();

        final ReceiverSettings first = ReceiverSettings.of(association, manager);
        final ReceiverSettings second = ReceiverSettings.of(association, manager);

        assertSame("the association's instances should share its settings", first, second);
        verify(manager, times(1)).findDicomSCPInstance(AE_TITLE, PORT);
        verify(manager, times(1)).getDicomObjectIdentifier(any(DicomSCPInstance.class));
    }

    @Test
    public void theSettingsAreTheReceivers() {
        final DicomSCPInstance receiver = receiver();
        final DicomSCPManager manager = managerFor(receiver);
        final DicomObjectIdentifier<XnatProjectdata> identifier = manager.getDicomObjectIdentifier(receiver);

        final ReceiverSettings settings = ReceiverSettings.of(association(), manager);

        assertTrue(settings.isCustomProcessing());
        assertTrue(settings.isDirectArchive());
        assertFalse(settings.isAnonymizationEnabled());
        assertEquals("append", settings.getDirectArchiveOverwrite());
        assertSame(identifier, settings.getIdentifier());
    }

    @Test
    public void eachAssociationReadsForItself() {
        final DicomSCPManager manager = managerFor(receiver());

        assertNotSame(ReceiverSettings.of(association(), manager), ReceiverSettings.of(association(), manager));
        verify(manager, times(2)).findDicomSCPInstance(AE_TITLE, PORT);
    }

    @Test
    public void anUnknownReceiverFailsEveryTime() {
        final DicomSCPManager manager = mock(DicomSCPManager.class);
        when(manager.findDicomSCPInstance(anyString(), anyInt())).thenReturn(Optional.empty());
        final Association association = association();

        assertThrows(IllegalArgumentException.class, () -> ReceiverSettings.of(association, manager));
        assertThrows(IllegalArgumentException.class, () -> ReceiverSettings.of(association, manager));
        verify(manager, times(2)).findDicomSCPInstance(AE_TITLE, PORT);
    }

    private static DicomSCPInstance receiver() {
        final DicomSCPInstance receiver = new DicomSCPInstance();
        receiver.setAeTitle(AE_TITLE);
        receiver.setPort(PORT);
        receiver.setCustomProcessing(true);
        receiver.setDirectArchive(true);
        receiver.setAnonymizationEnabled(false);
        receiver.setDirectArchiveOverwrite("append");
        return receiver;
    }

    @SuppressWarnings("unchecked")
    private static DicomSCPManager managerFor(final DicomSCPInstance receiver) {
        final DicomSCPManager manager = mock(DicomSCPManager.class);
        final DicomObjectIdentifier<XnatProjectdata> identifier = mock(DicomObjectIdentifier.class);
        when(manager.findDicomSCPInstance(AE_TITLE, PORT)).thenReturn(Optional.of(receiver));
        when(manager.getDicomObjectIdentifier(receiver)).thenReturn(identifier);
        return manager;
    }

    /** An association with a working property bag, received on AE_TITLE at PORT. */
    private static Association association() {
        final Association association = mock(Association.class);
        final Connection connection = mock(Connection.class);
        when(connection.getPort()).thenReturn(PORT);
        when(association.getLocalAET()).thenReturn(AE_TITLE);
        when(association.getConnection()).thenReturn(connection);
        final Map<Class<?>, Object> properties = new HashMap<>();
        when(association.getProperty(any(Class.class))).thenAnswer(invocation -> properties.get(invocation.<Class<?>>getArgument(0)));
        doAnswer(invocation -> properties.put(invocation.getArgument(0), invocation.getArgument(1))).when(association).setProperty(any(Class.class), any());
        return association;
    }
}
