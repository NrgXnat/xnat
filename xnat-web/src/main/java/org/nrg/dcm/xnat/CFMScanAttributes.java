/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.CFMScanAttributes
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import org.nrg.dcm.AttrDefs;
import org.nrg.dcm.MutableAttrDefs;

/**
 * DICOM to XNAT attribute mappings for CFM (Confocal Microscopy) scan data.
 * Maps confocal microscopy image-level DICOM attributes to XNAT scan properties.
 *
 * @author XNAT Development Team
 */
final class CFMScanAttributes {
    private CFMScanAttributes() {}    // no instantiation

    static public AttrDefs get() { return s; }

    static final private MutableAttrDefs s = new MutableAttrDefs(ImageScanAttributes.get());

    static {
    }
}
