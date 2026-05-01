/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.ECGSessionAttributes
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

final class ECGSessionAttributes {
    private ECGSessionAttributes() {}

    static public AttrDefs get() { return s; }

    static final private MutableAttrDefs s = new MutableAttrDefs(ImageSessionAttributes.get());

    static {
    }
}
