/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.OPTScanAttributes
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import org.dcm4che3.data.Tag;

import org.nrg.dcm.AttrDefs;
import org.nrg.dcm.MutableAttrDefs;

/**
 * optScanData attributes
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
class OPTScanAttributes {
    private OPTScanAttributes() {} // no instantiation

    static public AttrDefs get() { return s; }

    static final private MutableAttrDefs s = new MutableAttrDefs(ImageScanAttributes.get());

    static {
        s.add("type", new BasicCodedEntryAttributeIndex(new Integer[]{Tag.PerformedProtocolCodeSequence}, "99CZM", "1.0"));
        s.add(new VoxelResAttribute("parameters/voxelRes"));
        s.add(new ImageFOVAttribute("parameters/fov"));
        s.add("laterality", Tag.Laterality);
        s.add("parameters/illumination_wavelength", Tag.IlluminationWaveLength);
        s.add("parameters/illumination_power", Tag.IlluminationPower);
        s.add("parameters/imageType", Tag.ImageType);
    }
}
