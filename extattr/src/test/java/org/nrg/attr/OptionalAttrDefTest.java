/*
 * ExtAttr: org.nrg.attr.OptionalAttrDefTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class OptionalAttrDefTest {
    private final EvaluableAttrDef<String,Object,String> base = new SingleValueTextAttr<String>("foo", "bar");
    private final Map<String,String> VALUE_BAZ = Collections.singletonMap("bar", "baz");
    private final Map<String,String> VALUE_YAK = Collections.singletonMap("bar", "yak");
    private final Map<String,String> NO_VALUE = Collections.singletonMap("foo", "bar");
    private final String YAYAYA = "ya ya ya";

    /**
     * Test method for {@link org.nrg.attr.OptionalAttrDef#wrap(org.nrg.attr.EvaluableAttrDef)}.
     * @throws ExtAttrException 
     */
    @Test
    public void testWrap() throws ExtAttrException {
        final EvaluableAttrDef<String,Object,String> opt = OptionalAttrDef.wrap(base);

        final List<ExtAttrValue> vals = ImmutableList.copyOf(opt.foldl(Collections.singletonList(VALUE_BAZ)));
        assertEquals(1, vals.size());
        assertEquals("foo", vals.get(0).getName());
        assertEquals("baz", vals.get(0).getText());
    }

    /**
     * Test method for {@link org.nrg.attr.OptionalAttrDef#apply(java.lang.Object)}.
     */
    @Test
    public void testApplyValid() throws ExtAttrException {
        final EvaluableAttrDef<String,Object,String> opt = OptionalAttrDef.wrap(base);
        final List<ExtAttrValue> vals = ImmutableList.copyOf(opt.apply(YAYAYA));
        assertEquals(1, vals.size());
        assertEquals("foo", vals.get(0).getName());
        assertEquals(YAYAYA, vals.get(0).getText());
    }

    /**
     * Test method for {@link org.nrg.attr.OptionalAttrDef#apply(java.lang.Object)}.
     */
    @Test
    public void testApplyNull() throws ExtAttrException {
        final EvaluableAttrDef<String,Object,String> opt = OptionalAttrDef.wrap(base);
        final List<ExtAttrValue> vals = ImmutableList.copyOf(opt.apply(null));
        assertTrue(vals.isEmpty());
    }

    /**
     * Test method for {@link org.nrg.attr.OptionalAttrDef#foldl(java.lang.Object, java.util.Map)}.
     */
    @Test
    public void testFoldlValid() throws ExtAttrException {
        final EvaluableAttrDef<String,Object,String> opt = OptionalAttrDef.wrap(base);
        final String a0 = opt.foldl(null, VALUE_YAK);
        assertEquals("yak", a0);

        final String a1 = opt.foldl(a0, VALUE_YAK);
        assertEquals("yak", a1);
        
        final String a2 = opt.foldl(a1, NO_VALUE);
        assertEquals("yak", a2);
    }

    /**
     * Test method for {@link org.nrg.attr.OptionalAttrDef#foldl(java.lang.Object, java.util.Map)}.
     */
    @Test
    public void testFoldlConflict() throws ExtAttrException {
        final EvaluableAttrDef<String,Object,String> opt = OptionalAttrDef.wrap(base);
        final String a0 = opt.foldl(null, VALUE_YAK);
        assertEquals("yak", a0);

        final String a1 = opt.foldl(a0, VALUE_BAZ);
        assertEquals("yak", a1);
    }    

    /**
     * Test method for {@link org.nrg.attr.OptionalAttrDef#start()}.
     */
    @Test
    public void testStart() {
        final EvaluableAttrDef<String,Object,String> opt = OptionalAttrDef.wrap(base);
        assertEquals(base.start(), opt.start());
    }

}
