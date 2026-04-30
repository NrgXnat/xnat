/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.XCSessionAttributes
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
 * DICOM to XNAT attribute mappings for XC (Real-time Video Photography) session data.
 * Maps photography session-level DICOM attributes to XNAT session properties.
 *
 * @author XNAT Development Team
 */
final class XCSessionAttributes {
    private XCSessionAttributes() {}    // no instantiation

    static public AttrDefs get() { return s; }

    static final private MutableAttrDefs s = new MutableAttrDefs(ImageSessionAttributes.get());

    static {
    }
}
