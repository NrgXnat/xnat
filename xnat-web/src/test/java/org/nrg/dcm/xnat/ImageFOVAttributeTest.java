/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.ImageFOVAttributeTest
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
import static org.nrg.dcm.NamedAttributes.Cols;
import static org.nrg.dcm.NamedAttributes.Rows;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.nrg.attr.AbstractExtAttrDef;
import org.nrg.attr.ExtAttrException;
import org.nrg.attr.ExtAttrValue;
import org.nrg.dcm.DicomAttributeIndex;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;


public class ImageFOVAttributeTest {	
    /**
     * Test method for {@link org.nrg.dcm.xnat.ImageFOVAttribute#ImageFOVAttribute(java.lang.String)}.
     */
    @Test
    public void testImageFOVAttribute() {
        final XnatAttrDef imageFOV = new ImageFOVAttribute("fov");
        assertEquals(ImmutableSet.of(Rows, Cols), imageFOV.getAttrs());
    }

    /**
     * Test method for {@link org.nrg.dcm.xnat.ImageFOVAttribute#convert(java.util.Map)}.
     */
    @Test
    public void testConvertMapOfDicomAttributeIndexString() throws Exception {
        final XnatAttrDef fovDef = new ImageFOVAttribute("fov");

        final Iterable<ExtAttrValue> fovseq = AbstractExtAttrDef.foldl(fovDef, Collections.singleton(ImmutableMap.of(Rows, "64", Cols, "80")));
        final ExtAttrValue fov = fovseq.iterator().next();
        assertEquals(ImmutableMap.of("x", "80", "y", "64"), fov.getAttrs());
        assertNull(fov.getText());
    }
    
    @Test
    public void testAggregateWithNullValues() throws ExtAttrException {
        final ImageFOVAttribute fovdef = new ImageFOVAttribute("fov");
        final Map<?,Integer> m0 = fovdef.foldl(Collections.<Object,Integer>emptyMap(), ImmutableMap.<DicomAttributeIndex,String>of());
        assertTrue(m0.isEmpty());
        final Map<?,Integer> m1 = fovdef.foldl(m0, ImmutableMap.of(Rows, "64", Cols, "96"));
        assertFalse(m1.isEmpty());
        final Map<?,Integer> m2 = fovdef.foldl(m1,  ImmutableMap.<DicomAttributeIndex,String>of());
        assertEquals(m1, m2);
        assertEquals(ImmutableMap.of("x", "96", "y", "64"), Iterables.getOnlyElement(fovdef.apply(m2)).getAttrs());
    }
}
