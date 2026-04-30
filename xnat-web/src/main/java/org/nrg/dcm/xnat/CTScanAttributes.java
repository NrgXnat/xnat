/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.CTScanAttributes
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.gs.collections.impl.tuple.AbstractImmutableEntry;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.Tag;
import org.nrg.attr.ConversionFailureException;
import org.nrg.attr.ExtAttrValue;
import org.nrg.attr.TransformingExtAttrDef;
import org.nrg.dcm.AttrDefs;
import org.nrg.dcm.MutableAttrDefs;
import org.nrg.session.BeanBuilder;
import org.nrg.xdat.bean.XnatContrastbolusv2Bean;
import org.nrg.xdat.bean.XnatCtscandataFocalspotBean;
import org.nrg.xdat.bean.base.BaseElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.nrg.dcm.DicomAttributes.CONTRAST_BOLUS_SEQUENCE;
import static org.nrg.dcm.DicomAttributes.CT_CONVOLUTION_KERNEL;
import static org.nrg.dcm.DicomAttributes.CT_DATA_COLLECTION_DIAMETER;
import static org.nrg.dcm.DicomAttributes.CT_DISTANCE_SOURCE_TO_DETECTOR;
import static org.nrg.dcm.DicomAttributes.CT_DIVOL;
import static org.nrg.dcm.DicomAttributes.CT_ESTIMATED_DOSE_SAVING;
import static org.nrg.dcm.DicomAttributes.CT_EXPOSURE_MODULATION_TYPE;
import static org.nrg.dcm.DicomAttributes.CT_FILTER_TYPE;
import static org.nrg.dcm.DicomAttributes.CT_FOCAL_SPOTS;
import static org.nrg.dcm.DicomAttributes.CT_GANTRY_DETECTOR_TILT;
import static org.nrg.dcm.DicomAttributes.CT_KVP;
import static org.nrg.dcm.DicomAttributes.CT_ROTATION_DIRECTION;
import static org.nrg.dcm.DicomAttributes.CT_SINGLE_COLLIMATION_WIDTH;
import static org.nrg.dcm.DicomAttributes.CT_SPIRAL_PITCH_FACTOR;
import static org.nrg.dcm.DicomAttributes.CT_TABLE_FEED_PER_ROTATION;
import static org.nrg.dcm.DicomAttributes.CT_TABLE_HEIGHT;
import static org.nrg.dcm.DicomAttributes.CT_TABLE_SPEED;
import static org.nrg.dcm.DicomAttributes.CT_TOTAL_COLLIMATION_WIDTH;
import static org.nrg.dcm.DicomAttributes.RESCALE_INTERCEPT;
import static org.nrg.dcm.DicomAttributes.RESCALE_SLOPE;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
final class CTScanAttributes {
    private CTScanAttributes() {} // no instantiation

    static public AttrDefs get() { return s; }

    static final private MutableAttrDefs s = new MutableAttrDefs(ImageScanAttributes.get());

    static {
        s.add(new VoxelResAttribute("parameters/voxelRes"));
        s.add(new OrientationAttribute("parameters/orientation"));
        s.add("parameters/rescale/intercept", RESCALE_INTERCEPT);
        s.add("parameters/rescale/slope", RESCALE_SLOPE);
        s.add("parameters/kvp", CT_KVP);
        s.add("parameters/acquisitionNumber", Tag.AcquisitionNumber);
        s.add("parameters/imageType", Tag.ImageType);
        s.add("parameters/options", Tag.ScanOptions);
        s.add("parameters/collectionDiameter", CT_DATA_COLLECTION_DIAMETER);
        s.add("parameters/distanceSourceToDetector", CT_DISTANCE_SOURCE_TO_DETECTOR);
        s.add("parameters/distanceSourceToPatient", Tag.DistanceSourceToPatient);
        s.add("parameters/gantryTilt", CT_GANTRY_DETECTOR_TILT);
        s.add("parameters/tableHeight", CT_TABLE_HEIGHT);
        // Neurologica CereTom produces noncompliant DICOM. Thanks, Neurologica.
        s.add(TransformingExtAttrDef.wrap(new XnatAttrDef.Text("parameters/rotationDirection", CT_ROTATION_DIRECTION), "CCW", "CC"));
        s.add(new ExposureTimeAttribute("parameters/exposureTime", "CT", Tag.CTExposureSequence));
        s.add(new XRayTubeCurrentAttribute("parameters/xrayTubeCurrent", "CT", Tag.CTExposureSequence));
        s.add(new ExposureAttribute("parameters/exposure", "CT", Tag.CTExposureSequence));
        s.add("parameters/filter", CT_FILTER_TYPE);
        s.add("parameters/generatorPower", Tag.GeneratorPower);
        s.add("parameters/focalSpots/focalSpot", CT_FOCAL_SPOTS);	// requires additional processing from bean builder
        s.add("contrasts/contrastBolusV2", Tag.ContrastBolusAgent);
        s.add("contrasts/contrastBolusV2", CONTRAST_BOLUS_SEQUENCE);
        s.add("parameters/convolutionKernel", CT_CONVOLUTION_KERNEL);
        s.add("parameters/collimationWidth/single", CT_SINGLE_COLLIMATION_WIDTH);
        s.add("parameters/collimationWidth/total", CT_TOTAL_COLLIMATION_WIDTH);
        s.add("parameters/tableSpeed", CT_TABLE_SPEED);
        s.add("parameters/tableFeedPerRotation", CT_TABLE_FEED_PER_ROTATION);
        s.add("parameters/pitchFactor", CT_SPIRAL_PITCH_FACTOR);
        s.add(new XnatAttrDef.ValueWithAttributes("parameters/estimatedDoseSaving", CT_ESTIMATED_DOSE_SAVING,
                "modulation", CT_EXPOSURE_MODULATION_TYPE));
        s.add("parameters/ctDIvol", CT_DIVOL);
        s.add("parameters/derivation", Tag.DerivationDescription);
        s.add(new ImageFOVAttribute("parameters/fov"));
    }

    private final static Set<String> nullValues = ImmutableSet.of("", "null");

    private final static String FOCAL_SPOTS = "parameters/focalSpots/focalSpot";
    private final static BeanBuilder focalSpotsBeanBuilder = new BeanBuilder() {
        private final static String FOCAL_SPOT_SEPARATOR = "\\\\";
        public Collection<BaseElement> buildBeans(final ExtAttrValue focalSpotsValue)
        throws ConversionFailureException {
            final Collection<BaseElement> beans = new ArrayList<>();
            final String focalSpots = focalSpotsValue.getText();
            if (null == focalSpots || nullValues.contains(focalSpots)) {
                return beans;
            }
            for (final String focalSpot : focalSpots.split(FOCAL_SPOT_SEPARATOR)) {
                final XnatCtscandataFocalspotBean focalSpotBean = new XnatCtscandataFocalspotBean();
                focalSpotBean.setFocalspot(focalSpot);
                beans.add(focalSpotBean);
            }
            return beans;
        }
    };

    private final static String CONTRAST_BOLUS = "contrasts/contrastBolusV2";
    private final static BeanBuilder contrastBolusBeanBuilder = new BeanBuilder() {
        public Collection<BaseElement> buildBeans(final ExtAttrValue contrastBolusValue) throws ConversionFailureException {
            final Collection<BaseElement> beans = new ArrayList<>();
            String contrastBolus = contrastBolusValue.getText();
            if(StringUtils.endsWith(contrastBolus, "-text")){
                contrastBolus = StringUtils.stripEnd(contrastBolus,"-text");
            }
            if (null == contrastBolus || nullValues.contains(contrastBolus)) {
                return beans;
            }
            final XnatContrastbolusv2Bean contrastBolusBean = new XnatContrastbolusv2Bean();
            contrastBolusBean.setAgent(contrastBolus);
            beans.add(contrastBolusBean);
            return beans;
        }
    };

    private final static Map<String,BeanBuilder> beanBuilders = ImmutableMap.ofEntries(
            new AbstractImmutableEntry<>(FOCAL_SPOTS, focalSpotsBeanBuilder),
            new AbstractImmutableEntry<>(CONTRAST_BOLUS, contrastBolusBeanBuilder));

    public final static Map<String,BeanBuilder> getBeanBuilders() { return beanBuilders; }
}
