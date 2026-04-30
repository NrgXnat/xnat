/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.RESPSessionAttributes
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import org.nrg.dcm.AttrDefs;
import org.nrg.dcm.MutableAttrDefs;

final class RESPSessionAttributes {
    private RESPSessionAttributes() {}

    static public AttrDefs get() { return s; }

    static final private MutableAttrDefs s = new MutableAttrDefs(ImageSessionAttributes.get());

    static {
    }
}
