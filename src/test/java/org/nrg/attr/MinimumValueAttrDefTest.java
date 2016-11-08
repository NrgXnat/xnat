/*
 * ExtAttr: org.nrg.attr.MinimumValueAttrDefTest
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
public class MinimumValueAttrDefTest {
    private final EvaluableAttrDef<String,?,Integer> base = new IntegerExtAttrDef<String>("attr", "key");
    
    /**
     * Test method for {@link org.nrg.attr.MinimumValueAttrDef#wrap(org.nrg.attr.EvaluableAttrDef)}.
     */
    @Test
    public void testWrap() {
        final MinimumValueAttrDef<String,?,Integer> min = MinimumValueAttrDef.wrap(base);
        assertTrue(min instanceof ExtAttrDef<?>);
    }

    /**
     * Test method for {@link org.nrg.attr.MinimumValueAttrDef#start()}.
     */
    @Test
    public void testStart() {
        final MinimumValueAttrDef<String,?,Integer> min = MinimumValueAttrDef.wrap(base);
        assertEquals(base.start(), min.start());
    }

    /**
     * Test method for {@link org.nrg.attr.MinimumValueAttrDef#foldl(java.lang.Comparable, java.util.Map)}.
     */
    @Test
    public void testFoldl() throws ExtAttrException {
        @SuppressWarnings("unchecked")
        final MinimumValueAttrDef<String,Integer,Integer> min = (MinimumValueAttrDef<String,Integer,Integer>)MinimumValueAttrDef.wrap(base);
        assertNull(min.foldl(null, Collections.singletonMap("nokey", 3)));
        assertEquals(Integer.valueOf(3), min.foldl(null, Collections.singletonMap("key", 3)));
        assertEquals(Integer.valueOf(3), min.foldl(3, Collections.singletonMap("key", 4)));
        assertEquals(Integer.valueOf(2), min.foldl(3, Collections.singletonMap("key", 2)));
    }

    /**
     * Test method for {@link org.nrg.attr.MinimumValueAttrDef#apply(java.lang.Comparable)}.
     */
    @Test
    public void testApplyA() throws ExtAttrException {
        final MinimumValueAttrDef<String,?,Integer> min = MinimumValueAttrDef.wrap(base);
        final List<ExtAttrValue> vs = Lists.newArrayList(min.apply(-2));   
        assertEquals(1, vs.size());
        assertEquals(Integer.valueOf(-2), Integer.valueOf(vs.get(0).getText()));
    }

}
