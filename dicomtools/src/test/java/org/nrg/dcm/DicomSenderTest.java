/*
 * dicomtools: org.nrg.dcm.DicomSenderTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nrg.dicomtools.builders.NetworkApplicationEntityBuilder;
import org.nrg.dicomtools.builders.NetworkConnectionBuilder;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests {@link DicomSender} against an in-process dcm4che3 storage SCP. The local and remote AEs are built exactly the
 * way XNAT's callers build them, so this covers the full path from AE construction through to the C-STORE response.
 */
public class DicomSenderTest {

    private static final String LOCAL_AE         = "XNAT_TEST";
    private static final String REMOTE_AE        = "TEST_SCP";
    private static final String SOP_INSTANCE_UID = "1.2.3.4.1";

    private final Map<String, Attributes> stored = Collections.synchronizedMap(new HashMap<>());

    private Device                   scp;
    private ExecutorService          executor;
    private ScheduledExecutorService scheduledExecutor;
    private int                      port;
    private boolean                  rejectStores;

    @Before
    public void startScp() throws Exception {
        try (final ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }

        final Connection connection = new Connection("dicom", "localhost", port);

        final ApplicationEntity ae = new ApplicationEntity(REMOTE_AE);
        ae.setAssociationAcceptor(true);
        ae.addConnection(connection);
        ae.addTransferCapability(new TransferCapability(null, "*", TransferCapability.Role.SCP, "*"));

        executor = Executors.newCachedThreadPool();
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

        scp = new Device("test-scp");
        scp.addConnection(connection);
        scp.addApplicationEntity(ae);
        scp.setExecutor(executor);
        scp.setScheduledExecutor(scheduledExecutor);

        final DicomServiceRegistry registry = new DicomServiceRegistry();
        registry.addDicomService(new BasicCEchoSCP());
        registry.addDicomService(new BasicCStoreSCP("*") {
            @Override
            protected void store(final Association association, final PresentationContext pc, final Attributes request, final PDVInputStream data, final Attributes response) throws IOException {
                if (rejectStores) {
                    throw new DicomServiceException(Status.OutOfResources, "No room at the inn");
                }
                stored.put(request.getString(Tag.AffectedSOPInstanceUID), data.readDataset(pc.getTransferSyntax()));
            }
        });
        scp.setDimseRQHandler(registry);
        scp.bindConnections();
    }

    @After
    public void stopScp() {
        if (scp != null) {
            scp.unbindConnections();
        }
        if (executor != null) {
            executor.shutdown();
        }
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
    }

    /**
     * The regression test for the association failures: the sender has to build a device that owns the local AE's
     * connections, give it executors, and set the called AE title on the association request. Any one of those missing
     * fails the send before a single object reaches the PACS.
     */
    @Test
    public void testSendsObjectToRemoteAe() throws Exception {
        final DicomSender sender = buildSender();
        try {
            sender.send(dicomObject(SOP_INSTANCE_UID));
        } finally {
            sender.close();
        }

        assertThat(stored).containsOnlyKeys(SOP_INSTANCE_UID);
        assertThat(stored.get(SOP_INSTANCE_UID).getString(Tag.PatientName)).isEqualTo("Test^Patient");
    }

    /**
     * Each send() negotiates its own presentation context, so sending more than one object has to work as well.
     */
    @Test
    public void testSendsMultipleObjects() throws Exception {
        final DicomSender sender = buildSender();
        try {
            sender.send(dicomObject("1.2.3.4.1"));
            sender.send(dicomObject("1.2.3.4.2"));
        } finally {
            sender.close();
        }

        assertThat(stored).containsOnlyKeys("1.2.3.4.1", "1.2.3.4.2");
    }

    /**
     * A status the remote AE returns to reject the object has to fail the send: a rejected C-STORE completes normally
     * as far as the association is concerned, so it's only the response status that distinguishes it from a success.
     */
    @Test
    public void testFailsWhenRemoteAeRejectsObject() throws Exception {
        rejectStores = true;

        final DicomSender sender = buildSender();
        try {
            assertThatThrownBy(() -> sender.send(dicomObject(SOP_INSTANCE_UID))).isInstanceOf(IOException.class)
                                                                                .hasMessageContaining("A700")
                                                                                .hasMessageContaining(SOP_INSTANCE_UID);
        } finally {
            sender.close();
        }

        assertThat(stored).isEmpty();
    }

    private DicomSender buildSender() {
        final ApplicationEntity localAE = new NetworkApplicationEntityBuilder().setAETitle(LOCAL_AE)
                                                                              .setTransferCapability(new TransferCapability(null, UID.MRImageStorage, TransferCapability.Role.SCU, UID.ImplicitVRLittleEndian))
                                                                              .setNetworkConnection(new NetworkConnectionBuilder().build())
                                                                              .setAssociationInitiator()
                                                                              .build();

        final ApplicationEntity remoteAE = new NetworkApplicationEntityBuilder().setAETitle(REMOTE_AE)
                                                                               .setNetworkConnection(new NetworkConnectionBuilder().setHostname("localhost").setPort(port).build())
                                                                               .build();

        return new DicomSender(localAE, remoteAE);
    }

    private static Attributes dicomObject(final String sopInstanceUid) {
        final Attributes attributes = new Attributes();
        attributes.setString(Tag.SOPClassUID, VR.UI, UID.MRImageStorage);
        attributes.setString(Tag.SOPInstanceUID, VR.UI, sopInstanceUid);
        attributes.setString(Tag.StudyInstanceUID, VR.UI, "1.2.3.4");
        attributes.setString(Tag.SeriesInstanceUID, VR.UI, "1.2.3.4.0");
        attributes.setString(Tag.PatientName, VR.PN, "Test^Patient");
        attributes.setString(Tag.PatientID, VR.LO, "TEST_PATIENT");
        return attributes;
    }
}
