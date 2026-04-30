/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.MagneticFieldStrengthAttribute
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import org.dcm4che3.data.Tag;
import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.dcm.FixedDicomAttributeIndex;
import org.nrg.dcm.xnat.XnatAttrDef.Real;

/**
 * Corrects common problems in MR magnetic field strength:
 *  * some scanners use Gauss instead of Tesla
 *  * rounds to nearest 0.5 T
 *  
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class MagneticFieldStrengthAttribute
extends Real implements XnatAttrDef {
    private static final float GAUSS_THRESH = 500;  // bigger than this? must be in Gauss
    public static final DicomAttributeIndex MAGNETIC_FIELD_STRENGTH = new FixedDicomAttributeIndex(Tag.MagneticFieldStrength);

    public MagneticFieldStrengthAttribute() {
        super("fieldStrength", MAGNETIC_FIELD_STRENGTH);
    }

    /**
     * Some scanners use Gauss for magnetic field units even
     * though the standard requires Tesla. 
     * @param f magnetic field strength in either Gauss or Tesla
     * @return magnetic field strength in T (we hope)
     */
    private double degauss(final double f) {
        return f >= GAUSS_THRESH ? (f/10000.) : f;
    }

    /**
     * Round to the nearest 0.5
     * @param f double value
     * @return value rounded to the nearest 0.5
     */
    private double roundToHalf(final double f) {
        return Math.round(f*2.0)/2.0;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.RealExtAttrDef#valueOf(java.lang.String)
     */
    @Override
    public Double valueOf(final String s) {
        return roundToHalf(degauss(Double.valueOf(s)));
    }
    
    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AbstractExtAttrDef#apply(java.lang.Double)
     * 
     * Another in-principle-unnecessary redefinition to avoid AbstractMethodError.
     *
    @Override
    public Iterable<ExtAttrValue> apply(final Double d) throws ExtAttrException {
        return super.apply(d);
    }
    */
}
