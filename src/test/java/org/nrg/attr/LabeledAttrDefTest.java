/*
 * ExtAttr: org.nrg.attr.LabeledAttrDefTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

/**
 * 
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class LabeledAttrDefTest {
    /**
     * Test method for {@link org.nrg.attr.ExtAttrDefString.Labeled#convert(java.util.Map)}.
     */
    @Test
    public final void testLabeledConvert() throws ExtAttrException {
        final EvaluableAttrDef<NativeAttr,String,?> twa =
                new TextWithAttrsAttrDef<NativeAttr,String>("attributes", NativeAttr.A,
                        ImmutableMap.of("B", NativeAttr.B, "C", NativeAttr.C), false,
                        Collections.<NativeAttr>emptySet());
        final EvaluableAttrDef<NativeAttr,String,?> labeled = LabeledAttrDef.create(twa, "main", "A");
        final Iterable<ExtAttrValue> vals = labeled.foldl(ExtAttrDefTest.valslist);
        final ExtAttrValue val = Iterables.getOnlyElement(vals);
        assertEquals(ExtAttrDefTest.A_VALUE, val.getText());
        assertEquals("A", val.getAttrs().get("main"));
        assertEquals(ExtAttrDefTest.B_VALUE, val.getAttrs().get("B"));
        assertEquals(ExtAttrDefTest.C_VALUE, val.getAttrs().get("C"));
        assertNull(val.getAttrs().get("D"));
    }
}
