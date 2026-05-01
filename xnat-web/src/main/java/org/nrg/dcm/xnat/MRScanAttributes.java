/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.MRScanAttributes
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.dcm4che2.data.Tag;

import org.nrg.attr.ConversionFailureException;
import org.nrg.attr.ExtAttrValue;
import org.nrg.dcm.AttrDefs;
import org.nrg.dcm.MutableAttrDefs;
import org.nrg.session.BeanBuilder;
import org.nrg.xdat.bean.XnatContrastbolusv2Bean;
import org.nrg.xdat.bean.base.BaseElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.nrg.dcm.DicomAttributes.*;

/**
 * mrScanData attributes
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
class MRScanAttributes {
    private MRScanAttributes() {} // no instantiation

    static public AttrDefs get() { return s; }

    static final private MutableAttrDefs s = new MutableAttrDefs(ImageScanAttributes.get());

    static {
        s.add(new VoxelResAttribute("parameters/voxelRes"));
        s.add(new OrientationAttribute("parameters/orientation"));
        s.add("coil", MR_RECEIVE_COIL_NAME);
        s.add(new MagneticFieldStrengthAttribute());
        s.add(new XnatAttrDef.Real("parameters/tr", MR_REPETITION_TIME));
        s.add(new MREchoTimeAttribute());
        s.add(new XnatAttrDef.Real("parameters/ti", MR_INVERSION_TIME));
        s.add(new XnatAttrDef.Real("parameters/flip", MR_FLIP_ANGLE));
        s.add("parameters/sequence", Tag.SequenceName);
        s.add("parameters/imageType", Tag.ImageType);
        s.add("parameters/scanSequence", Tag.ScanningSequence);
        s.add("parameters/seqVariant", Tag.SequenceVariant);
        s.add("parameters/scanOptions", Tag.ScanOptions);
        s.add("parameters/acqType", Tag.MRAcquisitionType);
        s.add(new XnatAttrDef.Real("parameters/pixelBandwidth", MR_PIXEL_BANDWIDTH));
        s.add(new ImageFOVAttribute("parameters/fov"));
        s.add("contrasts/contrastBolusV2", Tag.ContrastBolusAgent);
        s.add("contrasts/contrastBolusV2", CONTRAST_BOLUS_SEQUENCE);

        s.add("parameters/diffusion/bValues", MR_DIFF_B_VALUES);
        s.add("parameters/diffusion/directionality", MR_DIFF_DIRECTION);
        s.add("parameters/diffusion/orientations", MR_DIFF_ORIENTATION);
        s.add("parameters/diffusion/anisotropyType", MR_DIFF_ANISOTROPY_TYPE);
    }

    private final static Set<String> nullValues = ImmutableSet.of("", "null");

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
    private final static Map<String,BeanBuilder> beanBuilders = ImmutableMap.of(CONTRAST_BOLUS, contrastBolusBeanBuilder);

    public final static Map<String,BeanBuilder> getBeanBuilders() { return beanBuilders; }

}
