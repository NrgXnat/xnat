/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.PAScanAttributes
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import org.dcm4che2.data.Tag;
import org.nrg.dcm.AttrDefs;
import org.nrg.dcm.MutableAttrDefs;

/**
 * DICOM to XNAT attribute mappings for PA (Photoacoustic Imaging) scan data.
 * Maps photoacoustic imaging acquisition-level DICOM attributes to XNAT scan properties.
 *
 * @author XNAT Development Team
 */
final class PAScanAttributes {
    private PAScanAttributes() {}    // no instantiation

    static public AttrDefs get() { return s; }

    static final private MutableAttrDefs s = new MutableAttrDefs(ImageScanAttributes.get());

    static {
    }
}
