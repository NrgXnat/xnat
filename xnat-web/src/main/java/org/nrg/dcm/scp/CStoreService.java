/*
 * web: org.nrg.dcm.scp.CStoreService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.scp;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.dcm.utils.StreamWrapper;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.archive.GradualDicomImporter;
import org.nrg.xnat.archive.ImportScope;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;

import javax.inject.Provider;
import java.io.IOException;

@Slf4j
public class CStoreService extends BasicCStoreSCP {
    private static final String PhilipsPrivateCXImageStorage = "1.3.46.670589.2.4.1.1";
    private static final String PhilipsPrivateVolumeStorage = "1.3.46.670589.5.0.1";
    private static final String PhilipsPrivate3DObjectStorage = "1.3.46.670589.5.0.2";
    private static final String PhilipsPrivate3DObject2Storage = "1.3.46.670589.5.0.2.1";
    private static final String PhilipsPrivateSurfaceStorage = "1.3.46.670589.5.0.3";
    private static final String PhilipsPrivateSurface2Storage = "1.3.46.670589.5.0.3.1";
    private static final String PhilipsPrivateCompositeObjectStorage = "1.3.46.670589.5.0.4";
    private static final String PhilipsPrivateMRCardioProfile = "1.3.46.670589.5.0.7";
    private static final String PhilipsPrivateMRCardio = "1.3.46.670589.5.0.8";
    private static final String PhilipsPrivateCTSyntheticImageStorage = "1.3.46.670589.5.0.9";
    private static final String PhilipsPrivateMRSyntheticImageStorage = "1.3.46.670589.5.0.10";
    private static final String PhilipsPrivateMRCardioAnalysisStorage = "1.3.46.670589.5.0.11";
    private static final String PhilipsPrivateCXSyntheticImageStorage = "1.3.46.670589.5.0.12";
    private static final String PhilipsPrivateGyroscanMRSpectrum = "1.3.46.670589.11.0.0.12.1";
    private static final String PhilipsPrivateGyroscanMRSerieData = "1.3.46.670589.11.0.0.12.2";
    private static final String PhilipsPrivateMRExamcardStorage = "1.3.46.670589.11.0.0.12.4";
    private static final String PhilipsPrivateSpecializedXAStorage = "1.3.46.670589.2.3.1.1";

    private static final String[] CUIDS = { // Full list in PS 3.4, Annex B.5
        UID.ComputedRadiographyImageStorage,
        UID.DigitalXRayImageStorageForPresentation,
        UID.DigitalXRayImageStorageForProcessing,
        UID.DigitalMammographyXRayImageStorageForPresentation,
        UID.DigitalMammographyXRayImageStorageForProcessing,
        UID.DigitalIntraOralXRayImageStorageForPresentation,
        UID.DigitalIntraOralXRayImageStorageForProcessing,
        UID.CTImageStorage, UID.EnhancedCTImageStorage,
        UID.UltrasoundMultiFrameImageStorage, UID.MRImageStorage,
        UID.EnhancedMRImageStorage,
        UID.MRSpectroscopyStorage,
        UID.UltrasoundImageStorage,
        UID.SecondaryCaptureImageStorage,
        UID.MultiFrameSingleBitSecondaryCaptureImageStorage,
        UID.MultiFrameGrayscaleByteSecondaryCaptureImageStorage,
        UID.MultiFrameGrayscaleWordSecondaryCaptureImageStorage,
        UID.MultiFrameTrueColorSecondaryCaptureImageStorage,
        UID.TwelveLeadECGWaveformStorage,
        UID.GeneralECGWaveformStorage,
        UID.AmbulatoryECGWaveformStorage,
        UID.HemodynamicWaveformStorage,
        UID.CardiacElectrophysiologyWaveformStorage,
        UID.BasicVoiceAudioWaveformStorage,
        UID.GrayscaleSoftcopyPresentationStateStorage,
        UID.ColorSoftcopyPresentationStateStorage,
        UID.PseudoColorSoftcopyPresentationStateStorage,
        UID.BlendingSoftcopyPresentationStateStorage,
        UID.XRayAngiographicImageStorage,
        UID.EnhancedXAImageStorage,
        UID.XRayRadiofluoroscopicImageStorage,
        UID.EnhancedXRFImageStorage,
        UID.XRay3DAngiographicImageStorage,
        UID.XRay3DCraniofacialImageStorage,
        UID.NuclearMedicineImageStorage,
        UID.RawDataStorage,
        UID.SpatialRegistrationStorage,
        UID.SpatialFiducialsStorage,
        UID.DeformableSpatialRegistrationStorage,
        UID.SegmentationStorage,
        UID.RealWorldValueMappingStorage,
        UID.VLEndoscopicImageStorage,
        UID.VideoEndoscopicImageStorage,
        UID.VLMicroscopicImageStorage,
        UID.VideoMicroscopicImageStorage,
        UID.VLSlideCoordinatesMicroscopicImageStorage,
        UID.VLPhotographicImageStorage,
        UID.VideoPhotographicImageStorage,
        UID.OphthalmicPhotography8BitImageStorage,
        UID.OphthalmicPhotography16BitImageStorage,
        UID.OphthalmicTomographyImageStorage,
        UID.StereometricRelationshipStorage,
        UID.BasicTextSRStorage,
        UID.EnhancedSRStorage,
        UID.ComprehensiveSRStorage,
        UID.ProcedureLogStorage,
        UID.MammographyCADSRStorage,
        UID.KeyObjectSelectionDocumentStorage,
        UID.ChestCADSRStorage,
        UID.XRayRadiationDoseSRStorage,
        UID.EncapsulatedPDFStorage,
        UID.PositronEmissionTomographyImageStorage,
        UID.RTImageStorage,
        UID.RTDoseStorage,
        UID.RTStructureSetStorage,
        UID.RTBeamsTreatmentRecordStorage,
        UID.RTPlanStorage,
        UID.RTBrachyTreatmentRecordStorage,
        UID.RTTreatmentSummaryRecordStorage,
        UID.RTIonPlanStorage,
        UID.RTIonBeamsTreatmentRecordStorage,
        UID.EnhancedMRColorImageStorage, // Support for enhanced DICOM color images
        UID.PrivateSiemensCSANonImageStorage, // Siemens proprietary; we get this sometimes
        PhilipsPrivateCXImageStorage, // Philips proprietary. Thanks, Philips.
        PhilipsPrivateVolumeStorage, PhilipsPrivate3DObjectStorage,
        PhilipsPrivate3DObject2Storage, PhilipsPrivateSurfaceStorage,
        PhilipsPrivateSurface2Storage,
        PhilipsPrivateCompositeObjectStorage,
        PhilipsPrivateMRCardioProfile, PhilipsPrivateMRCardio,
        PhilipsPrivateCTSyntheticImageStorage,
        PhilipsPrivateMRSyntheticImageStorage,
        PhilipsPrivateMRCardioAnalysisStorage,
        PhilipsPrivateCXSyntheticImageStorage,
        PhilipsPrivateGyroscanMRSpectrum,
        PhilipsPrivateGyroscanMRSerieData, PhilipsPrivateMRExamcardStorage,
        PhilipsPrivateSpecializedXAStorage,
            "1.2.840.10008.5.1.4.1.1.78.5", //Adding new additions to the DICOM standard
            "1.2.840.10008.5.1.4.1.1.9.5.1",
            "1.2.840.10008.5.1.4.1.1.130",
            "1.2.840.10008.5.1.4.1.1.13.1.3",
            "1.2.840.10008.5.1.4.1.1.88.68",
            "1.2.840.10008.5.1.4.1.1.11.6",
            "1.2.840.10008.5.1.4.1.1.11.10",
            "1.2.840.10008.5.1.4.1.1.11.9",
            "1.2.840.10008.5.1.4.1.1.11.11",
            "1.2.840.10008.5.1.4.1.1.78.3",
            "1.2.840.10008.5.1.4.1.1.81.1",
            "1.2.840.10008.5.1.4.1.1.128.1",
            "1.2.840.10008.5.1.4.1.1.88.69",
            "1.2.840.10008.5.1.4.1.1.11.8",
            "1.2.840.10008.5.1.4.1.1.88.71",
            "1.2.840.10008.5.1.4.1.1.78.7",
            "1.2.840.10008.5.1.4.1.1.66.6",
            "1.2.840.10008.5.1.4.1.1.79.1",
            "1.2.840.10008.5.1.4.1.1.13.1.5",
            "1.2.840.10008.5.1.4.1.1.14.1",
            "1.2.840.10008.5.1.4.34.10",
            "1.2.840.10008.5.1.4.1.1.30",
            "1.2.840.10008.5.1.4.1.1.77.1.5.6",
            "1.2.840.10008.5.1.4.1.1.9.6.1",
            "1.2.840.10008.5.1.4.1.1.66.5",
            "1.2.840.10008.5.1.4.1.1.481.10",
            "1.2.840.10008.5.1.4.1.1.88.74",
            "1.2.840.10008.5.1.4.1.1.77.1.5.5",
            "1.2.840.10008.5.1.4.1.1.77.1.5.7",
            "1.2.840.10008.5.1.4.1.1.104.2",
            "1.2.840.10008.5.1.4.1.1.88.75",
            "1.2.840.10008.5.1.4.1.1.4.4",
            "1.2.840.10008.5.1.4.1.1.78.2",
            "1.2.840.10008.5.1.4.1.1.2.2",
            "1.2.840.10008.5.1.4.1.1.77.1.5.8",
            "1.2.840.10008.5.1.4.1.1.78.1",
            "1.2.840.10008.5.1.4.1.1.80.1",
            "1.2.840.10008.5.1.4.1.1.481.11",
            "1.2.840.10008.5.1.4.1.1.14.2",
            "1.2.840.10008.5.1.4.1.1.11.5",
            "1.2.840.10008.5.1.4.1.1.77.1.6",
            "1.2.840.10008.5.1.4.1.1.131",
            "1.2.840.10008.5.1.4.1.1.11.7",
            "1.2.840.10008.5.1.4.1.1.200.2",
            "1.2.840.10008.5.1.4.1.1.13.1.4",
            "1.2.840.10008.5.1.4.1.1.9.4.2",
            "1.2.840.10008.5.1.4.1.1.88.73",
            "1.2.840.10008.5.1.4.34.7",
            "1.2.840.10008.5.1.4.1.1.78.4",
            "1.2.840.10008.5.1.4.1.1.78.8",
            "1.2.840.10008.5.1.4.1.1.104.3",
            "1.2.840.10008.5.1.4.1.1.68.1",
            "1.2.840.10008.5.1.4.1.1.90.1",
            "1.2.840.10008.5.1.4.1.1.78.6",
            "1.2.840.10008.5.1.4.1.1.68.2",
            "1.2.840.10008.5.1.4.1.1.6.2",
            "1.2.840.10008.5.1.4.1.1.88.34",
            "1.2.840.10008.5.1.4.1.1.88.72",
            "1.2.840.10008.5.1.4.1.1.82.1",
            "1.2.840.10008.5.1.4.1.1.88.70",
            "1.2.840.10008.5.1.4.1.1.88.35", };

    private final DicomObjectIdentifier<XnatProjectdata> identifier;
    private final Provider<UserI> userProvider;
    private DicomFileNamer namer = null;
    private final DicomSCPManager _manager;


    public CStoreService(final DicomObjectIdentifier<XnatProjectdata> identifier,
                         final Provider<UserI> userProvider, final DicomSCPManager manager) {
        super(CUIDS);
        this.identifier = identifier;
        this.userProvider = userProvider;
        this._manager = manager;
    }

    public CStoreService(final DicomObjectIdentifier<XnatProjectdata> identifier,
            final UserI user, final DicomSCPManager manager) {
        this(identifier, () -> user, manager);
    }

    /**
     * Adapted from StorageService, with a few tweaks for better compliance with
     * the standard: (see PS 3.7, section 9.3.1.2) + standard requires UID
     * fields in C-STORE-RSP + standard requires Data Set Type (Null) in
     * response
     */
    @Override
    protected void store(Association as, PresentationContext pc, Attributes rq,
                        PDVInputStream data, Attributes rsp) throws IOException {
        try {
            doCStore(as, data, pc.getTransferSyntax());
            rsp.setInt(Tag.Status, VR.US, Status.Success);
        } catch (final DicomServiceException e) {
            // Already carries its own status; dcm4che's registry will answer with it.
            throw e;
        } catch (final Throwable t) {
            // Answer, never throw. dcm4che's DicomServiceRegistry catches only DicomServiceException,
            // so anything else propagates to Association.onIOException, which closes the socket with
            // no C-STORE-RSP at all -- while PS3.7 9.1.1.1.9 says the Status "shall be included by the
            // Performing DIMSE Service User in the response/confirmation". Writing it into the response
            // dcm4che already built keeps the UID fields and Data Set Type that PS3.7 9.3.1.2 requires.
            //
            // This swallows a dead socket too, which is deliberate: tryWriteDimseRSP reports a failed
            // write rather than throwing, and the decoder's next read ends the association anyway, so
            // there is nothing to gain by distinguishing the two here.
            final int status = CStoreOutcome.statusFor(t);
            log.error("C-STORE operation failed for SOP instance {}, responding {}; see cause for the full chain",
                      rq.getString(Tag.AffectedSOPInstanceUID), String.format("%04XH", status), t);
            CStoreOutcome.applyTo(rsp, status, t);
        }
    }

    /**
     * Set the DicomFileNamer to be used for naming stored DICOM files.
     * @param namer
     * @return this
     */
    public CStoreService setNamer(final DicomFileNamer namer) {
        this.namer = namer;
        return this;
    }
    
    /**
     * The import scope of an association, created with its first instance and kept on the association: its
     * instances read the routing rules, archive processors and preferences once (see {@link ImportScope}). The
     * association's instances all arrive on its own thread, so no two of them ask at once.
     */
    private static ImportScope scopeOf(final Association association) {
        ImportScope scope = association.getProperty(ImportScope.class);
        if (scope == null) {
            scope = new ImportScope();
            association.setProperty(ImportScope.class, scope);
        }
        return scope;
    }

    private final Object identifySender(final Association association) {
        return new StringBuilder()
        .append(association.getRemoteAET()).append("@")
        .append(association.getSocket().getRemoteSocketAddress());
    }

    /**
     * Runs the import. Lets its failures out with their types and causes intact: {@link #store} is the
     * one place that classifies and logs them, so that a failure is reported once rather than caught,
     * logged, stringified into an {@link IOException} and then logged again by an outer handler.
     */
    private void doCStore(final Association as, final PDVInputStream dataStream, final String tsuid)
            throws IOException, ClientException, ServerException {
        final FileWriterWrapperI fw = new StreamWrapper(dataStream);
        final ReceiverSettings receiver = ReceiverSettings.of(as, _manager);
        final String directArchiveOverwrite = receiver.getDirectArchiveOverwrite();

        final ImmutableMap.Builder<String, Object> paramBuilder = ImmutableMap.<String, Object>builder()
                .put(GradualDicomImporter.SENDER_ID_PARAM, identifySender(as))
                .put(GradualDicomImporter.TSUID_PARAM, tsuid)
                .put(GradualDicomImporter.SENDER_AE_TITLE_PARAM, as.getRemoteAET())
                .put(GradualDicomImporter.RECEIVER_AE_TITLE_PARAM, as.getLocalAET())
                .put(GradualDicomImporter.RECEIVER_PORT_PARAM, as.getConnection().getPort())
                .put(GradualDicomImporter.CUSTOM_PROC_PARAM, receiver.isCustomProcessing())
                .put(GradualDicomImporter.DIRECT_ARCHIVE_PARAM, receiver.isDirectArchive())
                .put(URIManager.PREVENT_ANON, String.valueOf(!receiver.isAnonymizationEnabled()));
        if (directArchiveOverwrite != null) {
            paramBuilder.put(GradualDicomImporter.DIRECT_ARCHIVE_OVERWRITE_PARAM, directArchiveOverwrite);
        }
        final ImmutableMap<String, Object> parameters = paramBuilder.build();
        final GradualDicomImporter importer = new GradualDicomImporter(this,
                userProvider.get(), fw, parameters);
        importer.setIdentifier(receiver.getIdentifier());
        importer.setScope(scopeOf(as));
        if (null != namer) {
            importer.setNamer(namer);
        }
        importer.call();
    }


    
    public static final class Specifier {
        private final String aeTitle;
        private final Provider<UserI> userProvider;
        private final DicomObjectIdentifier<XnatProjectdata> identifier;
        private final DicomFileNamer namer;
        private final DicomSCPManager manager;

        public Specifier(final String aeTitle,
                final Provider<UserI> userProvider,
                final DicomObjectIdentifier<XnatProjectdata> identifier,
                final DicomFileNamer namer, final DicomSCPManager manager) {
            this.aeTitle = aeTitle;
            this.userProvider = userProvider;
            this.identifier = identifier;
            this.namer = namer;
            this.manager = manager;
        }
        
        public Specifier(final String aeTitle,
                final Provider<UserI> userProvider,
                final DicomObjectIdentifier<XnatProjectdata> identifier,
                final DicomSCPManager manager) {
            this(aeTitle, userProvider, identifier, null, manager);
        }
        
        public CStoreService build() {
            final CStoreService cstore = new CStoreService(identifier, userProvider, manager);
            if (null != namer) {
                cstore.setNamer(namer);
            }
            return cstore;
        }
        
        public String getAETitle() { return aeTitle; }
        
        public Provider<UserI> getUserProvider() { return userProvider; }

        public DicomObjectIdentifier<XnatProjectdata> getIdentifier() { return identifier; }

        public DicomFileNamer getNamer() { return namer; }
    }
}
