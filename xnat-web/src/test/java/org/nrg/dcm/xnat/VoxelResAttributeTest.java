/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.VoxelResAttributeTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.nrg.dcm.DicomAttributes.PIXEL_SPACING;
import static org.nrg.dcm.DicomAttributes.SLICE_THICKNESS;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;
import org.nrg.attr.AbstractExtAttrDef;
import org.nrg.attr.ExtAttrException;
import org.nrg.attr.ExtAttrValue;
import org.nrg.dcm.DicomAttributeIndex;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

/**
 * 
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class VoxelResAttributeTest {
    private ExtAttrValue getValue(final XnatAttrDef def, Map<DicomAttributeIndex,String> m) throws ExtAttrException {
        final Iterable<ExtAttrValue> vs = AbstractExtAttrDef.foldl(def, Collections.singletonList(m));
        final Iterator<ExtAttrValue> vsi = vs.iterator();
        assertTrue(vsi.hasNext());
        final ExtAttrValue v = vsi.next();
        assertFalse(vsi.hasNext());
        return v;
    }

    @Test
    public void testConvertMapOfDicomAttributeIndexString() throws Exception {
        final XnatAttrDef voxelResDef = new VoxelResAttribute("voxelRes");
        final ExtAttrValue voxelRes = getValue(voxelResDef, ImmutableMap.of(PIXEL_SPACING, "1\\1.5", SLICE_THICKNESS, "2"));
        assertNull(voxelRes.getText());
        assertEquals(ImmutableMap.of("x", "1.0", "y", "1.5", "z", "2.0"), voxelRes.getAttrs());
    }

    @Test
    public void testConvertTextMapOfDicomAttributeIndexString() throws Exception {
        final XnatAttrDef voxelResDef = new VoxelResAttribute("voxelRes");
        final String text = getValue(voxelResDef, ImmutableMap.of(PIXEL_SPACING, "1\\1.5", SLICE_THICKNESS, "2")).getText();
        assertNull(text);
    }
    
    @Test
    public void testAggregateWithNullValues() throws ExtAttrException {
        final VoxelResAttribute vresdef = new VoxelResAttribute("voxelRes");
        final Map<String,Double> m0 = vresdef.foldl(Collections.<String,Double>emptyMap(), ImmutableMap.<DicomAttributeIndex,String>of());
        assertTrue(m0.isEmpty());
        final Map<String,Double> m1 = vresdef.foldl(m0, ImmutableMap.of(PIXEL_SPACING, "2\\1", SLICE_THICKNESS, "1.5"));
        assertFalse(m1.isEmpty());
        final Map<String,Double> m2 = vresdef.foldl(m1,  ImmutableMap.<DicomAttributeIndex,String>of());
        assertEquals(m1, m2);
        assertEquals(ImmutableMap.of("x", "2.0", "y", "1.0", "z", "1.5"), Iterables.getOnlyElement(vresdef.apply(m2)).getAttrs());
    }
}
