/*
 * framework: org.nrg.framework.utilities.GraphUtilsTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import org.junit.jupiter.api.Test;
import org.nrg.framework.utilities.GraphUtils.CyclicGraphException;

import java.util.*;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

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
        assertThat(s1).isNotNull().isNotEmpty().hasSize(3).containsExactly("A", "B", "C");

        final Map m2 = new LinkedHashMap();
        m2.put("A", Collections.EMPTY_LIST);
        m2.put("B", asMutableList(new String[]{"A"}));
        m2.put("C", asMutableList(new String[]{"B"}));
        final List s2 = GraphUtils.topologicalSort(m2);
        assertThat(s2).isNotNull().isNotEmpty().hasSize(3).containsExactly("A", "B", "C");

        final Map m3 = new LinkedHashMap();
        m3.put("A", Collections.EMPTY_LIST);
        m3.put("B", Collections.EMPTY_LIST);
        m3.put("C", asMutableList(new String[]{"A", "B"}));
        m3.put("D", asMutableList(new String[]{"C"}));
        final List s3 = GraphUtils.topologicalSort(m3);
        assertThat(s3).isNotNull().isNotEmpty().hasSize(4);
        assertThat((String) s3.getFirst()).isNotNull().isNotEmpty().isIn("A", "B");
        assertThat((String) s3.get(1)).isNotNull().isNotEmpty().isIn("A", "B");
        assertThat((String) s3.get(2)).isNotNull().isNotEmpty().isEqualTo("C");
        assertThat((String) s3.get(3)).isNotNull().isNotEmpty().isEqualTo("D");

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
            assertThat(sorted).isNotNull().isNotEmpty().containsExactly("A", "B");
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
        assertThat(s).isNotNull().isNotEmpty().hasSize(1).containsExactly("A");
    }

    private List asMutableList(final Object[] a) {
        return new ArrayList(Arrays.asList(a));
    }
}
