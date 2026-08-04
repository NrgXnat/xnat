package org.nrg.dcm;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.DataWriterAdapter;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DicomSender {
    private final Logger logger = LoggerFactory.getLogger(DicomSender.class);
    private final ApplicationEntity localAE;
    private final ApplicationEntity remoteAE;
    private final Device device;
    private final Connection remoteConnection;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduledExecutor;
    private Association association; // current Association

    public DicomSender(final ApplicationEntity localAE, final ApplicationEntity remoteAE) {
        this.localAE = localAE;
        this.remoteAE = remoteAE;

        this.device = new Device("DicomSender3Device");
        // The device has to own the local AE's connections before the AE can be added to it: dcm4che3's
        // Device.addApplicationEntity() rejects an AE that has connections belonging to another (or no) device.
        localAE.getConnections().forEach(this.device::addConnection);
        this.device.addApplicationEntity(localAE);

        // The device also requires executors: it uses them to run the association's reader and timeout tasks.
        this.executor = Executors.newCachedThreadPool();
        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        this.device.setExecutor(executor);
        this.device.setScheduledExecutor(scheduledExecutor);

        this.remoteConnection = new Connection();
        this.remoteConnection.setHostname(remoteAE.getConnections().get(0).getHostname());
        this.remoteConnection.setPort(remoteAE.getConnections().get(0).getPort());
    }

    // TLS initialize
    public void initTLS(final String keyStoreURL, final String keyStorePass, final String keyPass,
                        final String trustStoreURL, final String trustStorePass) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream kis = new FileInputStream(keyStoreURL)) {
            keyStore.load(kis, keyStorePass.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyPass.toCharArray());

        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (FileInputStream tis = new FileInputStream(trustStoreURL)) {
            trustStore.load(tis, trustStorePass.toCharArray());
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        setSSLContext(sslContext);
    }

    // Setup SSLContext
    public void setSSLContext(final SSLContext context) {
        if (context == null) throw new IllegalArgumentException("SSLContext cannot be null");
        localAE.getConnections().forEach(conn -> {
                    conn.setTlsProtocols("TLSv1.2", "TLSv1.3");
                    conn.setTlsCipherSuites(context.getSupportedSSLParameters().getCipherSuites());
                });
        remoteConnection.setTlsProtocols("TLSv1.2", "TLSv1.3");
        remoteConnection.setTlsCipherSuites(context.getSupportedSSLParameters().getCipherSuites());
    }

    // Send DICOM object
    public void send(Attributes dicomObject) throws IOException, InterruptedException, IncompatibleConnectionException, GeneralSecurityException {
        // create request
        AAssociateRQ rq = new AAssociateRQ();
        String sopClassUID = dicomObject.getString(org.dcm4che3.data.Tag.SOPClassUID);
        String sopInstanceUID = dicomObject.getString(org.dcm4che3.data.Tag.SOPInstanceUID);

        // add Presentation Context
        rq.addPresentationContext(new PresentationContext(1, sopClassUID, UID.ImplicitVRLittleEndian));

        // open Association. Each object negotiates its own presentation context, so any association left over from a
        // previous send() has to be released here rather than leaked: close() only ever released the last one.
        release();
        association = localAE.connect(remoteConnection, rq);

        final CStoreRSPHandler rspHandler = new CStoreRSPHandler(association.nextMessageID());

        association.cstore(sopClassUID, sopInstanceUID, 0, new DataWriterAdapter(dicomObject), UID.ImplicitVRLittleEndian, rspHandler);

        // Wait
        association.waitForOutstandingRSP();
    }

    /**
     * Releases the current association and shuts down the device's executors. This sender can't be used to send any
     * more objects once it's been closed.
     */
    public void close() {
        release();
        executor.shutdown();
        scheduledExecutor.shutdown();
    }

    private void release() {
        if (association != null && association.isReadyForDataTransfer()) {
            try {
                association.release();
            } catch (Exception e) {
                logger.warn("release interrupted", e);
            } finally {
                association = null;
            }
        }
    }

    public void abort() {
        if (association != null && association.isReadyForDataTransfer()) {
            try {
                association.abort();
            } catch (Exception e) {
                logger.warn("abort interrupted", e);
            } finally {
                association = null;
            }
        }
    }
}
