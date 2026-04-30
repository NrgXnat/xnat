/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.OBJScanAttributes
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
 * DICOM to XNAT attribute mappings for OBJ (Encapsulated Object Storage) scan data.
 * Maps 3D model-level DICOM attributes to XNAT scan properties.
 *
 * @author XNAT Development Team
 */
final class OBJScanAttributes {
    private OBJScanAttributes() {}    // no instantiation

    static public AttrDefs get() { return s; }

    static final private MutableAttrDefs s = new MutableAttrDefs(ImageScanAttributes.get());

    static {
    }
}
