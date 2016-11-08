/*
 * ExtAttr: org.nrg.attr.ExtAttrDefTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class ExtAttrDefTest {
    public final static String                        A_VALUE  = "a-value";
    public final static String                        B_VALUE  = "b-value";
    public final static String                        C_VALUE  = "c-value";
    public final static Map<NativeAttr, String>       values   = ImmutableMap.of(NativeAttr.A, A_VALUE, NativeAttr.B, B_VALUE, NativeAttr.C, C_VALUE);
    public final static List<Map<NativeAttr, String>> valslist = ImmutableList.of(values);

    /**
     * Test method for {@link AttributesOnlyAttrDef#foldl(Object, Map)}.
     */
    @Test
    public final void testAttributesOnlyConvert() throws ExtAttrException {
        final EvaluableAttrDef<NativeAttr, String, ?> ao = new AttributesOnlyAttrDef<>("ao", Collections.singletonMap("A", NativeAttr.A), false, Collections.<NativeAttr>emptySet());
        final Iterable<ExtAttrValue> vals = ao.foldl(valslist);
        final ExtAttrValue val = Iterables.getOnlyElement(vals);
        assertEquals(A_VALUE, val.getAttrs().get("A"));
        assertNull(val.getAttrs().get("B"));
    }

    /**
     * Test method for {@link TextWithAttrsAttrDef#foldl(Object, Map)}.
     */
    @Test
    public final void testTextWithAttributesConvert() throws ExtAttrException {
        final EvaluableAttrDef<NativeAttr, String, ?> twa =
                new TextWithAttrsAttrDef<>("A", NativeAttr.A,
                                           ImmutableMap.of("B", NativeAttr.B, "C", NativeAttr.C), false,
                                           Collections.<NativeAttr>emptySet());
        final Iterable<ExtAttrValue> vals = twa.foldl(valslist);
        final ExtAttrValue val = Iterables.getOnlyElement(vals);
        assertEquals(A_VALUE, val.getText());
        assertEquals(B_VALUE, val.getAttrs().get("B"));
        assertEquals(C_VALUE, val.getAttrs().get("C"));
        assertNull(val.getAttrs().get("D"));
    }

    /**
     * Test method for {@link AbstractExtAttrDef#getName()}.
     */
    @Test
    public final void testGetName() {
        final String name = "myAttr";
        final ExtAttrDef<NativeAttr> ead = new AbstractExtAttrDef<NativeAttr, String, Object>(name, new HashSet<NativeAttr>()) {
            public Object start() {
                throw new UnsupportedOperationException("start");
            }

            public Object foldl(Object o, Map<? extends NativeAttr, ? extends String> s) {
                throw new UnsupportedOperationException("foldl(o,s)");
            }

            public Iterable<ExtAttrValue> apply(final Object object) {
                throw new UnsupportedOperationException("apply");
            }
        };
        assertEquals(name, ead.getName());
    }

    /**
     * Test method for {@link TextWithAttrsAttrDef#getAttrs()}.
     */
    @Test
    public final void testGetAttrs() {
        final ExtAttrDef<NativeAttr> twa =
                new TextWithAttrsAttrDef<NativeAttr, String>("attributes", NativeAttr.A,
                                                             ImmutableMap.of("B", NativeAttr.B, "C", NativeAttr.C), false,
                                                             Collections.<NativeAttr>emptySet());
        final Collection<NativeAttr> attrs = twa.getAttrs();
        assertEquals(3, attrs.size());
        assertTrue(attrs.contains(NativeAttr.A));
        assertTrue(attrs.contains(NativeAttr.B));
        assertTrue(attrs.contains(NativeAttr.C));
        assertFalse(attrs.contains(NativeAttr.D));
    }
}
