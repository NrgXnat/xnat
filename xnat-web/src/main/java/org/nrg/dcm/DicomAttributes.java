/*
 * dicom-xnat-mx: org.nrg.dcm.DicomAttributes
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import org.dcm4che3.data.Tag;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class DicomAttributes {
    public static DicomAttributeIndex chain(final String name,
            final Integer tag, final Integer subsq) {
        return new ChainedDicomAttributeIndex(name,
                new Integer[]{tag},
                new Integer[]{Tag.SharedFunctionalGroupsSequence, null, subsq, null, tag},
                new Integer[]{Tag.PerFrameFunctionalGroupsSequence, null, subsq, null, tag});
    }

    public static final DicomAttributeIndex

    CT_CONVOLUTION_KERNEL = chain("CT_ConvolutionKernel", Tag.ConvolutionKernel, Tag.CTReconstructionSequence),
    CT_DATA_COLLECTION_DIAMETER = chain("CT_DataCollectionDiameter", Tag.DataCollectionDiameter, Tag.CTAcquisitionDetailsSequence),
    CT_DISTANCE_SOURCE_TO_DETECTOR = chain("CT_DistSourceToDetector", Tag.DistanceSourceToDetector, Tag.CTGeometrySequence),
    CT_DIVOL = chain("CT_DIvol", Tag.CTDIvol, Tag.CTExposureSequence),
    CT_ESTIMATED_DOSE_SAVING = chain("CT_EstimatedDoseSaving", Tag.EstimatedDoseSaving, Tag.CTExposureSequence),
    CT_EXPOSURE_MODULATION_TYPE = chain("CT_ExposureModulationType", Tag.ExposureModulationType, Tag.CTExposureSequence),
    CT_FILTER_TYPE = chain("CT_FilterType", Tag.FilterType, Tag.CTXRayDetailsSequence),
    CT_FOCAL_SPOTS = chain("CT_FocalSpots", Tag.FocalSpots, Tag.CTXRayDetailsSequence),
    CT_GANTRY_DETECTOR_TILT = chain("CT_GantryDetectorTilt", Tag.GantryDetectorTilt, Tag.CTAcquisitionDetailsSequence),
    CT_KVP = chain("CT_KVP", Tag.KVP, Tag.CTXRayDetailsSequence),
    CT_ROTATION_DIRECTION = chain("CT_RotationDirection", Tag.RotationDirection, Tag.CTAcquisitionDetailsSequence),
    CT_SINGLE_COLLIMATION_WIDTH = chain("CT_SingleCollimationWidth", Tag.SingleCollimationWidth, Tag.CTAcquisitionDetailsSequence),
    CT_SPIRAL_PITCH_FACTOR = chain("CT_SpiralPitchFactor", Tag.SpiralPitchFactor, Tag.CTTableDynamicsSequence),
    CT_TABLE_FEED_PER_ROTATION = chain("CT_TableFeed", Tag.TableFeedPerRotation, Tag.CTTableDynamicsSequence),
    CT_TABLE_HEIGHT = chain("CT_TableHeight", Tag.TableHeight, Tag.CTAcquisitionDetailsSequence),
    CT_TABLE_SPEED = chain("CT_TableSpeed", Tag.TableSpeed, Tag.CTTableDynamicsSequence),
    CT_TOTAL_COLLIMATION_WIDTH = chain("CT_TotalCollimationWidth", Tag.TotalCollimationWidth, Tag.CTAcquisitionDetailsSequence),

    MR_FLIP_ANGLE = chain("MR_FlipAngle", Tag.FlipAngle, Tag.MRTimingAndRelatedParametersSequence),
    MR_PIXEL_BANDWIDTH = chain("MR_PixelBandwidth", Tag.PixelBandwidth, Tag.MRImagingModifierSequence),
    MR_RECEIVE_COIL_NAME = chain("MR_ReceiveCoilName", Tag.ReceiveCoilName, Tag.MRReceiveCoilSequence),
    MR_REPETITION_TIME = chain("MR_RepetitionTime", Tag.RepetitionTime, Tag.MRTimingAndRelatedParametersSequence),
    MR_INVERSION_TIME = chain("MR_InversionTime", Tag.InversionTime, Tag.MRTimingAndRelatedParametersSequence),

    MR_DIFF_B_VALUES = chain("MR_Diff_b", Tag.DiffusionBValue, Tag.MRDiffusionSequence),
    MR_DIFF_DIRECTION = chain("MR_Diff_Direction", Tag.DiffusionDirectionality, Tag.MRDiffusionSequence),
    MR_DIFF_ORIENTATION = chain("MR_Diff_Orientation", Tag.DiffusionGradientOrientation, Tag.MRDiffusionSequence),
    MR_DIFF_ANISOTROPY_TYPE = chain("MR_Diff_AnisotropyType", Tag.DiffusionAnisotropyType, Tag.MRDiffusionSequence),

    IMAGE_ORIENTATION_PATIENT = chain("ImageOrientationPatient", Tag.ImageOrientationPatient, Tag.PlaneOrientationSequence),
    PIXEL_SPACING = chain("PixelSpacing", Tag.PixelSpacing, Tag.PixelMeasuresSequence),
    SLICE_THICKNESS = chain("SliceThickness", Tag.SliceThickness, Tag.PixelMeasuresSequence),
    RESCALE_INTERCEPT = chain("RescaleIntercept", Tag.RescaleIntercept, Tag.PixelValueTransformationSequence),
    RESCALE_SLOPE = chain("RescaleSlope", Tag.RescaleSlope, Tag.PixelValueTransformationSequence),

    PET_RADIOPHARMACEUTICAL               = new FixedDicomAttributeIndex("Radiopharmaceutical", Tag.RadiopharmaceuticalInformationSequence, 0, Tag.Radiopharmaceutical),
    PET_RADIOPHARMACEUTICAL_DOSE          = new FixedDicomAttributeIndex("RadiopharmaceuticalDose", Tag.RadiopharmaceuticalInformationSequence, 0, Tag.RadionuclideTotalDose),
    PET_RADIOPHARMACEUTICAL_STARTDATETIME = new FixedDicomAttributeIndex("RadiopharmaceuticalStartDateTime", Tag.RadiopharmaceuticalInformationSequence, 0, Tag.RadiopharmaceuticalStartDateTime),
    PET_RADIONUCLIDE_HALFLIFE             = new FixedDicomAttributeIndex("RadionuclideHalfLife", Tag.RadiopharmaceuticalInformationSequence, 0, Tag.RadionuclideHalfLife),
    CONTRAST_BOLUS_SEQUENCE = new FixedDicomAttributeIndex("ContrastBolus", Tag.ContrastBolusAgentSequence, 0, Tag.CodeMeaning);
}
