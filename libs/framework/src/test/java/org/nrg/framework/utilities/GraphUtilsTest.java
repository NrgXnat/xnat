/*
 * framework: org.nrg.framework.utilities.GraphUtilsTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import org.junit.Test;
import org.nrg.framework.utilities.GraphUtils.CyclicGraphException;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class GraphUtilsTest {
    /**
     * Test method for {@link GraphUtils#topologicalSort(Map)}.
     */
    @Test
    public void testTopologicalSort() {
        final Map m1 = new LinkedHashMap();
        m1.put("A", Collections.EMPTY_LIST);
        m1.put("B", Collections.EMPTY_LIST);
        m1.put("C", Collections.EMPTY_LIST);
        final List s1 = GraphUtils.topologicalSort(m1);
        assertEquals(3, s1.size());
        assertTrue(s1.contains("A"));
        assertTrue(s1.contains("B"));
        assertTrue(s1.contains("C"));

        final Map m2 = new LinkedHashMap();
        m2.put("A", Collections.EMPTY_LIST);
        m2.put("B", asMutableList(new String[]{"A"}));
        m2.put("C", asMutableList(new String[]{"B"}));
        final List s2 = GraphUtils.topologicalSort(m2);
        assertEquals(3, s2.size());
        assertEquals("A", s2.get(0));
        assertEquals("B", s2.get(1));
        assertEquals("C", s2.get(2));

        final Map m3 = new LinkedHashMap();
        m3.put("A", Collections.EMPTY_LIST);
        m3.put("B", Collections.EMPTY_LIST);
        m3.put("C", asMutableList(new String[]{"A", "B"}));
        m3.put("D", asMutableList(new String[]{"C"}));
        final List s3 = GraphUtils.topologicalSort(m3);
        assertEquals(4, s3.size());
        assertTrue("A".equals(s3.get(0)) || "B".equals(s3.get(0)));
        assertTrue("B".equals(s3.get(1)) || "A".equals(s3.get(1)));
        assertEquals("C", s3.get(2));
        assertEquals("D", s3.get(3));

        final Map m4 = new LinkedHashMap();
        m4.put("A", Collections.EMPTY_LIST);
        m4.put("B", asMutableList(new String[]{"A"}));
        m4.put("C", asMutableList(new String[]{"B", "D"}));
        m4.put("D", asMutableList(new String[]{"C"}));
        try {
            GraphUtils.topologicalSort(m4);
            fail("Expected CyclicGraphException");
        } catch (CyclicGraphException e) {
            final List sorted = (List) e.getPartialResult();
            assertEquals(2, sorted.size());
            assertEquals("A", sorted.get(0));
            assertEquals("B", sorted.get(1));
        }
    }

    /**
     * Test method for {@link GraphUtils#topologicalSort(Map)}.
     */
    @Test
    public void testTopologicalSortSelfEdge() {
        final Map m = new LinkedHashMap();
        m.put("A", asMutableList(new String[]{"A"}));
        final List s = GraphUtils.topologicalSort(m);
        assertEquals(1, s.size());
        assertEquals("A", s.get(0));
    }

    private List asMutableList(final Object[] a) {
        return new ArrayList(Arrays.asList(a));
    }
}
