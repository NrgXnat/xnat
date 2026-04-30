/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.XCScanAttributes
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
 * DICOM to XNAT attribute mappings for XC (Real-time Video Photography) scan data.
 * Maps photography-specific DICOM attributes to XNAT scan properties.
 *
 * @author XNAT Development Team
 */
final class XCScanAttributes {
    private XCScanAttributes() {}    // no instantiation

    static public AttrDefs get() { return s; }

    static final private MutableAttrDefs s = new MutableAttrDefs(ImageScanAttributes.get());

    static {
    }
}
