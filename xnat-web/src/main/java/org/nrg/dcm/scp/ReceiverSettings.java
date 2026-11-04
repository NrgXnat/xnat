/*
 * web: org.nrg.dcm.scp.ReceiverSettings
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.scp;

import org.dcm4che3.net.Association;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.DicomObjectIdentifier;

/**
 * What the instances of a C-STORE association need from the receiver they arrive at: its processing settings and
 * the identifier for its instances. Read with the association's first instance and kept on the association, so an
 * association looks the receiver up once and builds one identifier. A change to the receiver applies from the next
 * association. The association's instances all arrive on its own thread, so no two of them read at once.
 */
final class ReceiverSettings {
    static ReceiverSettings of(final Association association, final DicomSCPManager manager) {
        ReceiverSettings settings = association.getProperty(ReceiverSettings.class);
        if (settings == null) {
            settings = read(manager, association.getLocalAET(), association.getConnection().getPort());
            association.setProperty(ReceiverSettings.class, settings);
        }
        return settings;
    }

    private static ReceiverSettings read(final DicomSCPManager manager, final String aeTitle, final int port) {
        final DicomSCPInstance instance = manager.findDicomSCPInstance(aeTitle, port)
                                                 .orElseThrow(() -> new IllegalArgumentException("Unknown DicomSCPInstances with aeTitle '%s' and port %d".formatted(aeTitle, port)));
        return new ReceiverSettings(instance, manager.getDicomObjectIdentifier(instance));
    }

    private ReceiverSettings(final DicomSCPInstance instance, final DicomObjectIdentifier<XnatProjectdata> identifier) {
        _customProcessing       = instance.isCustomProcessing();
        _directArchive          = instance.isDirectArchive();
        _anonymizationEnabled   = instance.isAnonymizationEnabled();
        _directArchiveOverwrite = instance.getDirectArchiveOverwrite();
        _identifier             = identifier;
    }

    boolean isCustomProcessing() {
        return _customProcessing;
    }

    boolean isDirectArchive() {
        return _directArchive;
    }

    boolean isAnonymizationEnabled() {
        return _anonymizationEnabled;
    }

    String getDirectArchiveOverwrite() {
        return _directArchiveOverwrite;
    }

    DicomObjectIdentifier<XnatProjectdata> getIdentifier() {
        return _identifier;
    }

    private final boolean                                _customProcessing;
    private final boolean                                _directArchive;
    private final boolean                                _anonymizationEnabled;
    private final String                                 _directArchiveOverwrite;
    private final DicomObjectIdentifier<XnatProjectdata> _identifier;
}
