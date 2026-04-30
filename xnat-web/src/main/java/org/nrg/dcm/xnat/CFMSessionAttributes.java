/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.CFMSessionAttributes
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
 * DICOM to XNAT attribute mappings for CFM (Confocal Microscopy) session data.
 * Maps confocal microscopy procedure-level DICOM attributes to XNAT session properties.
 *
 * @author XNAT Development Team
 */
final class CFMSessionAttributes {
    private CFMSessionAttributes() {}    // no instantiation

    static public AttrDefs get() { return s; }

    static final private MutableAttrDefs s = new MutableAttrDefs(ImageSessionAttributes.get());

    static {
    }
}
