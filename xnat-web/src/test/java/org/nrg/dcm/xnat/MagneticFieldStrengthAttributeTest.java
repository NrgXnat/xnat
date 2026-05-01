/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.MagneticFieldStrengthAttributeTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;
import org.nrg.attr.AbstractExtAttrDef;
import org.nrg.attr.ConversionFailureException;
import org.nrg.attr.ExtAttrException;
import org.nrg.attr.ExtAttrValue;
import org.nrg.attr.NoUniqueValueException;
import org.nrg.dcm.DicomAttributeIndex;

import static org.nrg.dcm.xnat.MagneticFieldStrengthAttribute.*;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class MagneticFieldStrengthAttributeTest {
    private final XnatAttrDef fs = new MagneticFieldStrengthAttribute();

    private ExtAttrValue getValue(final XnatAttrDef def, Map<DicomAttributeIndex,String> m) throws ExtAttrException {
        final Iterable<ExtAttrValue> vs = AbstractExtAttrDef.foldl(def, Collections.singletonList(m));
        final Iterator<ExtAttrValue> vsi = vs.iterator();
        assertTrue(vsi.hasNext());
        final ExtAttrValue v = vsi.next();
        assertFalse(vsi.hasNext());
        return v;
    }

    @Test
    public void testName() throws ExtAttrException {
        final ExtAttrValue v = getValue(fs, Collections.singletonMap(MAGNETIC_FIELD_STRENGTH, "3.0"));
        assertEquals("fieldStrength", v.getName());
    }

    @Test
    public void testConvertTrivial() throws ExtAttrException {
        final ExtAttrValue v = getValue(fs, Collections.singletonMap(MAGNETIC_FIELD_STRENGTH, "1.5"));
        assertTrue(v.getAttrs().isEmpty());
        assertEquals("1.5", v.getText());
    }

    @Test
    public void testConvertRounding() throws ExtAttrException {
        final ExtAttrValue v1 = getValue(fs, Collections.singletonMap(MAGNETIC_FIELD_STRENGTH, "1.49997"));
        assertTrue(v1.getAttrs().isEmpty());
        assertEquals("1.5", v1.getText());

        final ExtAttrValue v2 = getValue(fs, Collections.singletonMap(MAGNETIC_FIELD_STRENGTH, "2.893"));
        assertTrue(v2.getAttrs().isEmpty());
        assertEquals("3.0", v2.getText());
    }

    @Test
    public void testConvertFromGauss() throws ExtAttrException {
        final ExtAttrValue v = getValue(fs, Collections.singletonMap(MAGNETIC_FIELD_STRENGTH, "14996"));
        assertTrue(v.getAttrs().isEmpty());
        assertEquals("1.5", v.getText());
    }

    @Test(expected = ConversionFailureException.class)
    public void testInvalidValue() throws ExtAttrException {
        getValue(fs, Collections.singletonMap(MAGNETIC_FIELD_STRENGTH, "foo"));
    }

    @Test(expected = NoUniqueValueException.class)
    public void testNullValue() throws ExtAttrException {
        getValue(fs, new HashMap<DicomAttributeIndex,String>());
    }
}
