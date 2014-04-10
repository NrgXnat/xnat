/**
 * Copyright (c) 2014 Washington University School of Medicine
 */
package org.nrg.attr;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

/**
 * @author Kevin A. Archie <karchie@wustl.edu>
 *
 */
public class TransformingExtAttrDefTest {

    private static Function<ExtAttrValue,ExtAttrValue> mapEAVal(final String k, final String v) {
        return new Function<ExtAttrValue,ExtAttrValue>() {
            public ExtAttrValue apply(final ExtAttrValue eav) {
                return k.equals(eav.getText()) ? new BasicExtAttrValue(eav.getName(), v, eav.getAttrs()) : eav;
            }
        };
    }
    
    private static Function<String,String> mapString(final String k, final String v) {
        return new Function<String,String>() {
            public String apply(final String v_) {
                return k.equals(v_) ? v : v_;
            }
        };
    }
    
    /**
     * Test method for {@link org.nrg.attr.TransformingExtAttrDef#wrap(org.nrg.attr.EvaluableAttrDef, com.google.common.base.Function)}.
     */
    @Test
    public void testWrapEvaluableAttrDefOfSVAFunctionOfExtAttrValueExtAttrValue() throws ExtAttrException {
        final EvaluableAttrDef<NativeAttr,?,?> a = new ConstantAttrDef<NativeAttr>("foo", "bar");
        final EvaluableAttrDef<NativeAttr,?,?> a1 = TransformingExtAttrDef.wrap(a, mapEAVal("bar", "baz"));
        assertEquals("baz", Iterables.getOnlyElement(a1.apply(null)).getText());
        final EvaluableAttrDef<NativeAttr,?,?> a2 = TransformingExtAttrDef.wrap(a, mapEAVal("baz", "yak"));
        assertEquals("bar", Iterables.getOnlyElement(a2.apply(null)).getText());       
    }

    /**
     * Test method for {@link org.nrg.attr.TransformingExtAttrDef#wrapTextFunction(org.nrg.attr.EvaluableAttrDef, com.google.common.base.Function)}.
     */
    @Test
    public void testWrapTextFunction() throws ExtAttrException {
        final EvaluableAttrDef<NativeAttr,?,?> a = new ConstantAttrDef<NativeAttr>("foo", "bar");
        final EvaluableAttrDef<NativeAttr,?,?> a1 = TransformingExtAttrDef.wrapTextFunction(a, mapString("bar", "baz"));
        assertEquals("baz", Iterables.getOnlyElement(a1.apply(null)).getText());
        final EvaluableAttrDef<NativeAttr,?,?> a2 = TransformingExtAttrDef.wrapTextFunction(a, mapString("baz", "yak"));
        assertEquals("bar", Iterables.getOnlyElement(a2.apply(null)).getText());       
    }

    /**
     * Test method for {@link org.nrg.attr.TransformingExtAttrDef#wrapTextMap(org.nrg.attr.EvaluableAttrDef, java.util.Map)}.
     */
    @Test
    public void testWrapTextMap() throws ExtAttrException {
        final EvaluableAttrDef<NativeAttr,?,?> a = new ConstantAttrDef<NativeAttr>("foo", "bar");
        final EvaluableAttrDef<NativeAttr,?,?> a1 = TransformingExtAttrDef.wrapTextMap(a, ImmutableMap.of("bar", "baz"));
        assertEquals("baz", Iterables.getOnlyElement(a1.apply(null)).getText());
        final EvaluableAttrDef<NativeAttr,?,?> a2 = TransformingExtAttrDef.wrapTextMap(a, ImmutableMap.of("baz", "yak"));
        assertEquals("bar", Iterables.getOnlyElement(a2.apply(null)).getText());       
    }

    /**
     * Test method for {@link org.nrg.attr.TransformingExtAttrDef#wrap(org.nrg.attr.EvaluableAttrDef, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testWrapEvaluableAttrDefOfSVAStringString() throws ExtAttrException {
        final EvaluableAttrDef<NativeAttr,?,?> a = new ConstantAttrDef<NativeAttr>("foo", "bar");
        final EvaluableAttrDef<NativeAttr,?,?> a1 = TransformingExtAttrDef.wrap(a, "bar", "baz");
        assertEquals("baz", Iterables.getOnlyElement(a1.apply(null)).getText());
        final EvaluableAttrDef<NativeAttr,?,?> a2 = TransformingExtAttrDef.wrap(a, "baz", "yak");
        assertEquals("bar", Iterables.getOnlyElement(a2.apply(null)).getText());       
    }

    /**
     * Test method for {@link org.nrg.attr.TransformingExtAttrDef#wrap(org.nrg.attr.EvaluableAttrDef, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testWrapEvaluableAttrDefOfSVAStringStringStringString()throws ExtAttrException {
        final EvaluableAttrDef<NativeAttr,?,?> a = new ConstantAttrDef<NativeAttr>("foo", "bar");
        final EvaluableAttrDef<NativeAttr,?,?> b = new ConstantAttrDef<NativeAttr>("foo", "ack");
        final EvaluableAttrDef<NativeAttr,?,?> a1 = TransformingExtAttrDef.wrap(a, "bar", "baz", "ack", "thpthpppt");
        final EvaluableAttrDef<NativeAttr,?,?> b1 = TransformingExtAttrDef.wrap(b, "bar", "baz", "ack", "thpthpppt");
        assertEquals("baz", Iterables.getOnlyElement(a1.apply(null)).getText());
        assertEquals("thpthpppt", Iterables.getOnlyElement(b1.apply(null)).getText());
        final EvaluableAttrDef<NativeAttr,?,?> a2 = TransformingExtAttrDef.wrap(a, "baz", "yak", "ack", "thpthpppt");
        assertEquals("bar", Iterables.getOnlyElement(a2.apply(null)).getText());       
    }
}
