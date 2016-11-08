/*
 * ExtAttr: org.nrg.attr.MaximumValueAttrDefTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class MaximumValueAttrDefTest {
    private final EvaluableAttrDef<String,?,Integer> base = new IntegerExtAttrDef<String>("attr", "key");
    
    /**
     * Test method for {@link org.nrg.attr.MaximumValueAttrDef#wrap(org.nrg.attr.EvaluableAttrDef)}.
     */
    @Test
    public void testWrap() {
        final MaximumValueAttrDef<String,?,Integer> min = MaximumValueAttrDef.wrap(base);
        assertTrue(min instanceof ExtAttrDef<?>);
    }

    /**
     * Test method for {@link org.nrg.attr.MaximumValueAttrDef#start()}.
     */
    @Test
    public void testStart() {
        final MaximumValueAttrDef<String,?,Integer> min = MaximumValueAttrDef.wrap(base);
        assertEquals(base.start(), min.start());
    }

    /**
     * Test method for {@link org.nrg.attr.MaximumValueAttrDef#foldl(java.lang.Comparable, java.util.Map)}.
     */
    @Test
    public void testFoldl() throws ExtAttrException {
        @SuppressWarnings("unchecked")
        final MaximumValueAttrDef<String,Integer,Integer> min = (MaximumValueAttrDef<String,Integer,Integer>)MaximumValueAttrDef.wrap(base);
        assertNull(min.foldl(null, Collections.singletonMap("nokey", 3)));
        assertEquals(Integer.valueOf(3), min.foldl(null, Collections.singletonMap("key", 3)));
        assertEquals(Integer.valueOf(3), min.foldl(3, Collections.singletonMap("key", 2)));
        assertEquals(Integer.valueOf(4), min.foldl(3, Collections.singletonMap("key", 4)));
    }

    /**
     * Test method for {@link org.nrg.attr.MaximumValueAttrDef#apply(java.lang.Comparable)}.
     */
    @Test
    public void testApplyA() throws ExtAttrException {
        final MaximumValueAttrDef<String,?,Integer> min = MaximumValueAttrDef.wrap(base);
        final List<ExtAttrValue> vs = Lists.newArrayList(min.apply(-2));   
        assertEquals(1, vs.size());
        assertEquals(Integer.valueOf(-2), Integer.valueOf(vs.get(0).getText()));
    }

}
