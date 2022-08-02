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
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.DicomServiceException;
import org.dcm4che2.net.PDVInputStream;
import org.dcm4che2.net.service.CStoreSCP;
import org.dcm4che2.net.service.DicomService;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.archive.GradualDicomImporter;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;

import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class CStoreService extends DicomService implements CStoreSCP {
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
        UID.GrayscaleSoftcopyPresentationStateStorageSOPClass,
        UID.ColorSoftcopyPresentationStateStorageSOPClass,
        UID.PseudoColorSoftcopyPresentationStateStorageSOPClass,
        UID.BlendingSoftcopyPresentationStateStorageSOPClass,
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
        UID.SiemensCSANonImageStorage, // Siemens proprietary; we get this sometimes
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

    public static final int SUCCESS = 0;
    public static final int REFUSED_OUT_OF_RESOURCES = 0xA700;
    public static final int ERROR_DATA_SET_SOP_CLASS_MISMATCH = 0xA900;
    public static final int ERROR_CANNOT_UNDERSTAND = 0xC000;
    public static final int WARNING_COERCION_DATA_ELEMENTS = 0xB000;
    public static final int WARNING_DATA_SET_SOP_CLASS_MISMATCH = 0xB007;
    public static final int WARNING_ELEMENTS_DISCARDED = 0xB006;

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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dcm4che2.net.service.CStoreSCP#cstore(org.dcm4che2.net.Association,
     * int, org.dcm4che2.data.DicomObject, org.dcm4che2.net.PDVInputStream,
     * java.lang.String)
     */
    /**
     * Adapted from StorageService, with a few tweaks for better compliance with
     * the standard: (see PS 3.7, section 9.3.1.2) + standard requires UID
     * fields in C-STORE-RSP + standard requires Data Set Type (Null) in
     * response
     */
    @Override
    public void cstore(final Association as, final int pcid,
            final DicomObject rq, final PDVInputStream dataStream,
            final String tsuid) throws DicomServiceException,IOException {
        final boolean includeUIDs = CommandUtils.isIncludeUIDinRSP();
        CommandUtils.setIncludeUIDinRSP(true);
        final DicomObject rsp = CommandUtils.mkRSP(rq, CommandUtils.SUCCESS);
        rsp.putInt(Tag.DataSetType, VR.US, 0x0101);
        doCStore(as, pcid, rq, dataStream, tsuid, rsp);
        as.writeDimseRSP(pcid, rsp);
        CommandUtils.setIncludeUIDinRSP(includeUIDs);
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
    
    private final Object identifySender(final Association association) {
        return new StringBuilder()
        .append(association.getRemoteAET()).append("@")
        .append(association.getSocket().getRemoteSocketAddress());
    }

    private void doCStore(final Association as, final int pcid,
            final DicomObject rq, final PDVInputStream dataStream,
            final String tsuid, final DicomObject rsp)
    throws DicomServiceException {
        final FileWriterWrapperI fw = new StreamWrapper(dataStream);
        try {
            try {
                boolean doCustomProcessing   = false;
                boolean directArchive        = false;
                boolean anonymizationEnabled = true;

                String aeTitle = as.getLocalAET();
                int port = as.getConnector().getPort();
                doCustomProcessing   = _manager.isCustomProcessing( aeTitle, port);
                directArchive        = _manager.isDirectArchive( aeTitle, port);
                anonymizationEnabled = _manager.isAnonymizationEnabled( aeTitle, port);

                final ImmutableMap<String, Object> parameters = ImmutableMap.<String, Object>builder()
                        .put(GradualDicomImporter.SENDER_ID_PARAM, identifySender(as))
                        .put(GradualDicomImporter.TSUID_PARAM, tsuid)
                        .put(GradualDicomImporter.SENDER_AE_TITLE_PARAM, as.getRemoteAET())
                        .put(GradualDicomImporter.RECEIVER_AE_TITLE_PARAM, as.getLocalAET())
                        .put(GradualDicomImporter.RECEIVER_PORT_PARAM, as.getConnector().getPort())
                        .put(GradualDicomImporter.CUSTOM_PROC_PARAM, doCustomProcessing)
                        .put(GradualDicomImporter.DIRECT_ARCHIVE_PARAM, directArchive)
                        .put(URIManager.PREVENT_ANON, String.valueOf(!anonymizationEnabled))
                        .build();
                final GradualDicomImporter importer = new GradualDicomImporter(this,
                        userProvider.get(), fw, parameters);
                importer.setIdentifier( _manager.getDicomObjectIdentifier( aeTitle, port));
                if (null != namer) {
                    importer.setNamer(namer);
                }
                importer.call();
            } catch (final ClientException e) {
                log.error("C-STORE operation failed", e);
                throw new DicomServiceException(rq, ERROR_CANNOT_UNDERSTAND,
                        e.getMessage());
            } catch (final ServerException e) {
                log.error("C-STORE operation failed", e);
                throw new DicomServiceException(rq, REFUSED_OUT_OF_RESOURCES,
                        e.getMessage());
            }
        } catch (DicomServiceException e) {
            throw e;
        } catch (final Throwable e) {
            // Don't let mysterious unchecked exceptions and errors through.
            log.error("C-STORE operation failed", e);
            throw new DicomServiceException(rq, REFUSED_OUT_OF_RESOURCES,
                    e.getMessage());
        }
    }

    private static final class StreamWrapper implements FileWriterWrapperI {
        private final InputStream in;

        StreamWrapper(final InputStream in) { this.in = in; }

        @Override
        public void write(File f) {
            throw new UnsupportedOperationException();
        }

        @Override
        public UPLOAD_TYPE getType() { return UPLOAD_TYPE.INBODY; }

        @Override
        public String getName() { return null; }

        @Override
        public String getNestedPath() {
            return null;
        }

        @Override
        public InputStream getInputStream() { return in; }

        @Override
        public void delete() {
            throw new UnsupportedOperationException();
        }
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
