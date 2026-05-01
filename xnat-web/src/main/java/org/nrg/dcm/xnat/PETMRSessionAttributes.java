/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.PETMRSessionAttributes
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
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
final class PETMRSessionAttributes {
    private PETMRSessionAttributes() {}    // no instantiation
    static public AttrDefs get() { return s; }

    static final private MutableAttrDefs s = new MutableAttrDefs(ImageSessionAttributes.get());
    
    static {
        s.add(new MagneticFieldStrengthAttribute());
    }
}
