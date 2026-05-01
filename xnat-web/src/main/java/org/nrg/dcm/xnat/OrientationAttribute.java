/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.OrientationAttribute
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import static org.nrg.dcm.DicomAttributes.IMAGE_ORIENTATION_PATIENT;

import java.util.Arrays;
import java.util.Map;

import org.nrg.attr.ConversionFailureException;
import org.nrg.attr.ExtAttrException;
import org.nrg.attr.ExtAttrValue;
import org.nrg.attr.NoUniqueValueException;
import org.nrg.dcm.DicomAttributeIndex;

/**
 * XNAT orientation (Sag|Cor|Tra) from DICOM orientation (R^6)
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
final class OrientationAttribute extends XnatAttrDef.Abstract<String> {
    final static String ORI_SAGITTAL = "Sag";
    final static String ORI_CORONAL = "Cor";
    final static String ORI_TRANSVERSE = "Tra";

    OrientationAttribute(final String name) {
        super(name, IMAGE_ORIENTATION_PATIENT);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AbstractExtAttrDef#apply(java.lang.String)
     */
    public Iterable<ExtAttrValue> apply(final String a) throws ExtAttrException {
        return applyString(a);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.EvaluableAttrDef#foldl(java.lang.Object, java.util.Map)
     */
    public String foldl(final String a, final Map<? extends DicomAttributeIndex,? extends String> m)
            throws ExtAttrException {
        final String dcmo = m.get(IMAGE_ORIENTATION_PATIENT);
        if (null == dcmo) {
            return a;
        }
        final String[] components = dcmo.split("\\\\");

        // Orientation : row cosine vector (x,y,z), then column
        // cosine vector (x,y,z)
        if (components.length != 6) {
            throw new ConversionFailureException(IMAGE_ORIENTATION_PATIENT, dcmo, getName(),
                    "cannot be translated to cosine vectors");
        }

        final double[] o = new double[6];
        try {
            for (int i = 0; i < 6; i++) {
                o[i] = Double.parseDouble(components[i]);
            }
        } catch (NumberFormatException e) {
            throw new ConversionFailureException(IMAGE_ORIENTATION_PATIENT, dcmo, getName(),
                    "cannot be translated to cosine vectors");
        }

        // consistency checks
        final double epsilon = 0.001;
        if (Math.abs(o[0] * o[3] + o[1] * o[4] + o[2] * o[5]) > 0.001) {
            throw new ConversionFailureException(IMAGE_ORIENTATION_PATIENT, dcmo, getName(),
                    "cosine vectors not orthogonal");
        }

        if ((Math.abs(1.0 - o[0] * o[0] - o[1] * o[1] - o[2] * o[2]) > epsilon)
                || (Math.abs(1.0 - o[3] * o[3] - o[4] * o[4] - o[5] * o[5]) > epsilon)) {
            throw new ConversionFailureException(IMAGE_ORIENTATION_PATIENT, dcmo, getName(),
                    "cosine vectors not normal");
        }

        // cross product gives direction of normal to image plane
        final double absNormalX = Math.abs(o[1] * o[5] - o[2] * o[4]);
        final double absNormalY = Math.abs(o[2] * o[3] - o[0] * o[5]);
        final double absNormalZ = Math.abs(o[0] * o[4] - o[1] * o[3]);

        // patient's body length is Z, ventral-dorsal is Y, and right-left is Z,
        // so image plane normal along X->sagittal, Y->coronal, Z->transverse
        final String orientation;
        if (absNormalX > absNormalY) {
            orientation = absNormalX > absNormalZ ? ORI_SAGITTAL : ORI_TRANSVERSE;
        } else {
            orientation = absNormalY > absNormalZ ? ORI_CORONAL : ORI_TRANSVERSE;
        }

        if (null == a || a.equals(orientation)) {
            return orientation;
        } else {
            throw new NoUniqueValueException(getName(), Arrays.asList(a, orientation));
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.Foldable#start()
     */
    public String start() { return null; }
}
