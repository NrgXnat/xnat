/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.MREchoTimeAttributeTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.nrg.attr.AbstractExtAttrDef;
import org.nrg.attr.ExtAttrException;
import org.nrg.attr.ExtAttrValue;
import org.nrg.dcm.DicomAttributeIndex;

import static org.nrg.dcm.Attributes.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class MREchoTimeAttributeTest {
    private List<ExtAttrValue> getValues(final XnatAttrDef def, Map<DicomAttributeIndex,String>...ms) throws ExtAttrException {
        final Iterable<ExtAttrValue> vs = AbstractExtAttrDef.foldl(def, Arrays.asList(ms));
        return Lists.newArrayList(vs);
    }

    /**
     * Test method for {@link org.nrg.dcm.xnat.MREchoTimeAttribute#MREchoTimeAttribute()}.
     */
    @Test
    public void testMREchoTimeAttribute() {
        final XnatAttrDef tedef = new MREchoTimeAttribute();
        assertEquals(Sets.newHashSet(EchoTime, EchoNumbers), tedef.getAttrs());
    }

    /**
     * Test method for {@link org.nrg.dcm.xnat.MREchoTimeAttribute#convertText(java.util.Map)}.
     */
    @Test
    public void testConvertTextMapOfDicomAttributeIndexString() throws Exception {
        final XnatAttrDef tedef = new MREchoTimeAttribute();
        final String text = getValues(tedef, ImmutableMap.of(EchoTime, "1500.0", EchoNumbers, "1")).get(0).getText();
        assertEquals("1500.0", text);
    }

    /**
     * Test method for {@link org.nrg.dcm.xnat.MREchoTimeAttribute#demultiplex(java.util.Map)}.
     */
    @Test
    public void testDemultiplex() throws Exception {
        final XnatAttrDef tedef = new MREchoTimeAttribute();
        final List<ExtAttrValue> vs = getValues(tedef,
                ImmutableMap.of(EchoTime, "1200.0", EchoNumbers, "1"),
                ImmutableMap.of(EchoTime, "1600.0", EchoNumbers, "2"));
        final ExtAttrValue v0 = vs.get(0);
        assertEquals("parameters/addParam", v0.getName());
        assertEquals("1200.0", v0.getText());
        assertEquals(ImmutableMap.of("name", "MultiEcho_TE1"), v0.getAttrs());
    }
}
