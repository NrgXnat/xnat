/*
 * dicomtools: org.nrg.dcm.DicomSender
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import javax.net.ssl.SSLContext;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;

import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DataWriterAdapter;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.TransferCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.nrg.dcm.DimseRSPStatusHandler.ServiceStatus;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *         Minimal DICOM Storage Service SCU
 */
public class DicomSender {
    // these three arrays lifted from DcmSnd
    private static final String[] IVLE_TS = {
            UID.ImplicitVRLittleEndian,
            UID.ExplicitVRLittleEndian,
            UID.ExplicitVRBigEndian,
            };

    private static final String[] EVLE_TS = {
            UID.ExplicitVRLittleEndian,
            UID.ImplicitVRLittleEndian,
            UID.ExplicitVRBigEndian,
            };

    private static final String[] EVBE_TS = {
            UID.ExplicitVRBigEndian,
            UID.ExplicitVRLittleEndian,
            UID.ImplicitVRLittleEndian,
            };

    private static final String DEVICE_NAME          = "DicomSender";
    private static final String THREAD_EXECUTOR_NAME = "DicomSender";


    private final Logger logger = LoggerFactory.getLogger(DicomSender.class);

    private       Association assoc;
    private final String[]    tsuids;

    private final int priority = 0;

    private final NetworkApplicationEntity localAE, remoteAE;
    private final Device device;

    public DicomSender(final NetworkApplicationEntity localAE, final NetworkApplicationEntity remoteAE) {
        this.localAE = localAE;
        this.remoteAE = remoteAE;

        device = new Device(DEVICE_NAME);
        device.setNetworkApplicationEntity(this.localAE);
        device.setNetworkConnection(localAE.getNetworkConnection());

        final Collection<String> tsuids = new HashSet<>();
        for (final TransferCapability tc : localAE.getTransferCapability()) {
            if (tc.isSCU()) {
                tsuids.addAll(Arrays.asList(tc.getTransferSyntax()));
            }
        }
        this.tsuids = tsuids.toArray(new String[0]);

        assoc = null;
    }

    /**
     * Initialize a TLS connection with the given security information
     *
     * @param keyStoreURL    Location of keystore
     * @param keyStorePass   Password for keystore
     * @param keyPass        Password for key
     * @param trustStoreURL  Location of trust store
     * @param trustStorePass Password for trust store
     * @throws IOException              When an error occurs reading or writing data.
     * @throws GeneralSecurityException When a security error occurs.
     */
    public void initTLS(final String keyStoreURL, final String keyStorePass, final String keyPass,
                        final String trustStoreURL, final String trustStorePass) throws IOException, GeneralSecurityException {
        final char[]   ksp        = null == keyStorePass ? null : keyStorePass.toCharArray();
        final KeyStore keyStore   = loadKeyStore(keyStoreURL, ksp);
        final char[]   tsp        = null == trustStorePass ? null : trustStorePass.toCharArray();
        final KeyStore trustStore = loadKeyStore(trustStoreURL, tsp);
        device.initTLS(keyStore, null == keyPass ? ksp : keyPass.toCharArray(), trustStore);
    }

    public void setSSLContext(final SSLContext context) {
        device.setSSLContext(context);
    }

    public String send(final DicomObject o, String tsuid)
            throws IOException, CStoreException {
        if (null == assoc || !assoc.isReadyForDataTransfer()) {
            try {
                assoc = connect();
            } catch (ConfigurationException e) {
                logger.error("association configuration failed", e);
                throw new IOException("unable to configure association: " + e.getMessage());
            } catch (InterruptedException e) {
                logger.error("unable to (re)open association", e);
                throw new IOException("thread interrupted opening association: " + e.getMessage());
            }
        }

        // TODO: error checking
        final String cuid = o.getString(Tag.SOPClassUID);
        final String iuid = o.getString(Tag.SOPInstanceUID);

        // TODO: select transfer syntax
        tsuid = selectTransferSyntax(tsuids, tsuid);

        final CStoreRSPHandler rsph = new CStoreRSPHandler();

        try {
            assoc.cstore(cuid, iuid, priority, new DataWriterAdapter(o), tsuid, rsph);
            assoc.waitForDimseRSP();
        } catch (InterruptedException e) {
            logger.error("send interrupted", e);
        }

        final ServiceStatus status = rsph.getStatus();
        if (ServiceStatus.SUCCESS == status) {
            return null;
        } else if (ServiceStatus.WARNING == status) {
            return rsph.toString();
        } else if (ServiceStatus.FAILURE == status) {
            throw new CStoreException(rsph.toString());
        } else {
            throw new RuntimeException("Undefined ServiceStatus " + status);
        }
    }


    public void close() {
        try {
            if (null != assoc) {
                assoc.release(false);
            }
        } catch (InterruptedException e) {
            logger.warn("release interrupted", e);
        }
    }

    public void abort() {
        assoc.abort();
    }

    private static InputStream openURI(final URI uri) throws IOException {
        final String scheme = uri.getScheme();
        if (null == scheme || "file".equals(scheme)) {
            return new FileInputStream(uri.getPath());
        } else if ("resource".equalsIgnoreCase(scheme)) {
            return DicomSender.class.getClassLoader().getResourceAsStream(uri.getPath());
        }
        try {
            return uri.toURL().openStream();
        } catch (MalformedURLException e) {
            // last-ditch attempt
            return new FileInputStream(uri.getPath());
        }
    }

    private static String toKeyStoreType(final String fname) {
        return null == fname ? null : fname.matches(".*\\.[pP]12\\Z") ? "PKCS12" : "JKS";
    }

    private static KeyStore loadKeyStore(final String uri, final char[] password)
            throws IOException, GeneralSecurityException {
        if (null == uri) {
            return null;
        }

        final KeyStore    key = KeyStore.getInstance(toKeyStoreType(uri));
        final InputStream is;
        try {
            is = openURI(new URI(uri));
        } catch (URISyntaxException e) {
            throw new FileNotFoundException("invalid keystore URI " + e.getMessage());
        }
        try {
            key.load(is, password);
            return key;
        } finally {
            is.close();
        }
    }

    /**
     * Lifted from DcmSnd
     *
     * @param available The available transfer syntaxes.
     * @param tsuid     The specified transfer syntax ID.
     * @return The specified transfer syntax.
     */
    private String selectTransferSyntax(String[] available, String tsuid) {
        if (UID.ImplicitVRLittleEndian.equals(tsuid)) {
            return selectTransferSyntax(available, IVLE_TS);
        }
        if (UID.ExplicitVRLittleEndian.equals(tsuid)) {
            return selectTransferSyntax(available, EVLE_TS);
        }
        if (UID.ExplicitVRBigEndian.equals(tsuid)) {
            return selectTransferSyntax(available, EVBE_TS);
        }
        return tsuid;
    }

    /**
     * Lifted from DcmSnd
     *
     * @param available The available transfer syntaxes.
     * @param tsuids    The specified transfer syntax IDs.
     * @return The first transfer syntax found matching one of the IDs.
     */
    private String selectTransferSyntax(final String[] available, final String[] tsuids) {
        for (final String tsuid : tsuids) {
            for (final String anAvailable : available) {
                if (anAvailable.equals(tsuid)) {
                    return anAvailable;
                }
            }
        }
        return null;
    }

    private Association connect() throws ConfigurationException, IOException, InterruptedException {
        return localAE.connect(remoteAE, new NewThreadExecutor(THREAD_EXECUTOR_NAME));
    }
}
