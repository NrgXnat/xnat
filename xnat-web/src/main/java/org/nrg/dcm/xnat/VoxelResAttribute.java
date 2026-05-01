/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.VoxelResAttribute
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import static org.nrg.dcm.DicomAttributes.PIXEL_SPACING;
import static org.nrg.dcm.DicomAttributes.SLICE_THICKNESS;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.nrg.attr.BasicExtAttrValue;
import org.nrg.attr.ConversionFailureException;
import org.nrg.attr.ExtAttrException;
import org.nrg.attr.ExtAttrValue;
import org.nrg.attr.NoUniqueValueException;
import org.nrg.dcm.DicomAttributeIndex;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
final class VoxelResAttribute extends XnatAttrDef.Abstract<Map<String,Double>> {
    VoxelResAttribute(final String name) {
        super(name, PIXEL_SPACING, SLICE_THICKNESS);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.Foldable#start()
     */
    public Map<String,Double> start() { return Maps.newLinkedHashMap(); }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.EvaluableAttrDef#foldl(java.lang.Object, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public Map<String,Double> foldl(final Map<String,Double> a,
            final Map<? extends DicomAttributeIndex,? extends String> m)
                    throws ExtAttrException {
        final String vrxy = m.get(PIXEL_SPACING), sliceThickness = m.get(SLICE_THICKNESS);
        if (null == vrxy || null == sliceThickness) {
            return a;
        }

        final String[] xy = vrxy.split("\\\\");
        if (2 != xy.length) {
            throw new ConversionFailureException(PIXEL_SPACING, vrxy,
                    "Expect two values separated by delimiter \\");
        }
        final double x, y, z;
        try {
            x = Double.parseDouble(xy[0]);
            y = Double.parseDouble(xy[1]);
        } catch (NumberFormatException e) {
            throw new ConversionFailureException(PIXEL_SPACING, vrxy,
                    getName(), "cannot be translated to real numbers");
        }
        try {
            z = Double.parseDouble(m.get(SLICE_THICKNESS));
        } catch (NumberFormatException e) {
            throw new ConversionFailureException(SLICE_THICKNESS,
                    m.get(SLICE_THICKNESS), getName(),
                    "cannot be translated to a real number");
        }   	
        final Map<String,Double> v = ImmutableMap.of("x", x, "y", y, "z", z);
        if (null == v) {
            return a;
        } else if (a.isEmpty() || a.equals(v)) {
            return v;
        } else {
            throw new NoUniqueValueException(getName(), Arrays.asList(a, v));
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.EvaluableAttrDef#apply(java.lang.Object)
     */
    public Iterable<ExtAttrValue> apply(final Map<String,Double> a) throws NoUniqueValueException {
        if (a.containsKey("x") && a.containsKey("y") && a.containsKey("z")) {
            return Collections.<ExtAttrValue>singletonList(new BasicExtAttrValue(getName(), null,
                    ImmutableMap.of("x", String.valueOf(a.get("x")),
                            "y", String.valueOf(a.get("y")),
                            "z", String.valueOf(a.get("z")))));
        } else {
            throw new NoUniqueValueException(getName(), a.entrySet());
        }
    }
}
