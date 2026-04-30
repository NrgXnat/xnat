/*
 * framework: org.nrg.framework.utilities.SortedSetsTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import java.util.*;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class SortedSetsTest {

    /**
     * Test method for {@link SortedSets#empty()}.
     */
    @Test
    public void testEmpty() {
        final SortedSet<Object> s = SortedSets.empty();
        assertThat(s).isEmpty();
    }

    @Test
    public void testEmptyHasNullComparator() {
        final SortedSet<Object> s = SortedSets.empty();
        assertThat(s.comparator()).isNull();
    }

    @Test
    public void testEmptyFirst() {
        assertThrows(NoSuchElementException.class, () -> {
            final SortedSet<Object> s = SortedSets.empty();
            s.getFirst();
        });
    }

    @Test
    public void testEmptyLast() {
        assertThrows(NoSuchElementException.class, () -> {
            final SortedSet<Object> s = SortedSets.empty();
            s.getLast();
        });
    }

    @Test
    public void testEmptyEquals() {
        final SortedSet<Object> s = SortedSets.empty();
        assertThat(s).isNotNull().isEmpty();
    }

    @Test
    public void testEmptyHeadSet() {
        final SortedSet<String> s  = SortedSets.empty();
        final SortedSet<String> ss = s.headSet("a");
        assertThat(ss).isEmpty();
    }

    @Test
    public void testEmptySubSet() {
        final SortedSet<String> s  = SortedSets.empty();
        final SortedSet<String> ss = s.subSet("a", "c");
        assertThat(ss).isEmpty();
    }

    @Test
    public void testEmptyTailSet() {
        final SortedSet<String> s  = SortedSets.empty();
        final SortedSet<String> ss = s.tailSet("a");
        assertThat(ss).isEmpty();
    }

    @Test
    public void testEmptyAdd() {
        assertThrows(UnsupportedOperationException.class, () -> {
            final SortedSet<Object> s = SortedSets.empty();
            s.add("a");
        });
    }

    @Test
    public void testEmptyAddAll() {
        assertThrows(UnsupportedOperationException.class, () -> {
            final SortedSet<Object> s = SortedSets.empty();
            s.addAll(Arrays.asList("a", "b"));
        });
    }

    @Test
    public void testEmptyContains() {
        final SortedSet<Object> s = SortedSets.empty();
        assertThat(s).isEmpty();
    }

    @Test
    public void testEmptyContainsAll() {
        final SortedSet<Object> s = SortedSets.empty();
        assertThat(s).doesNotContain("a", "b").isEmpty();
    }

    @Test
    public void testEmptyHashCode() {
        assertThat(SortedSets.empty().hashCode()).isEqualTo(0);
    }

    @Test
    public void testEmptyIterator() {
        assertThat(SortedSets.empty().iterator().hasNext()).isFalse();
    }

    @Test
    public void testEmptyRemove() {
        final SortedSet<Object> s = SortedSets.empty();
        assertThat(s.remove(null)).isFalse();
        assertThat(s.remove("a")).isFalse();
    }

    @Test
    public void testEmptyRemoveAll() {
        final SortedSet<Object> s = SortedSets.empty();
        assertThat(s.removeAll(Collections.emptyList())).isFalse();
        assertThat(s.removeAll(Arrays.asList("a", "b"))).isFalse();
    }

    @Test
    public void testEmptyRetainAll() {
        final SortedSet<Object> s = SortedSets.empty();
        assertThat(s.retainAll(Collections.emptyList())).isFalse();
        assertThat(s.retainAll(Arrays.asList("a", "b"))).isFalse();
    }

    @Test
    public void testEmptyToArray() {
        final SortedSet<Object> s = SortedSets.empty();
        assertThat(s.toArray()).hasSize(0);
        assertThat(s.toArray(new Object[0])).hasSize(0);
    }

    /**
     * Test method for {@link SortedSets#singleton(Comparable, Comparator)}.
     */
    @Test
    public void testSingletonTComparatorOfQsuperT() {
        // Use (and verify) reverse-alphabetical ordering
        final SortedSet<String> s = SortedSets.singleton("b", Comparator.reverseOrder());
        assertThat(s).isNotNull().isNotEmpty().hasSize(1).contains("b");
        assertThat(s.headSet("a")).containsExactlyElementsOf(s);
        assertThat(s.headSet("b")).isEmpty();
        assertThat(s.headSet("c")).isEmpty();
        assertThat(s.tailSet("a")).isEmpty();
        assertThat(s.tailSet("b")).containsExactlyElementsOf(s);
        assertThat(s.tailSet("c")).containsExactlyElementsOf(s);
    }

    /**
     * Test method for {@link SortedSets#singleton(Comparable, Comparator)}.
     */
    @Test
    public void testSingletonT() {
        final SortedSet<String> s = SortedSets.singleton("a");
        assertThat(s).isNotNull().isNotEmpty().hasSize(1).containsExactly("a").doesNotContain("b");
        assertThat(s.comparator()).isNull();

        final String[] ss = s.toArray(new String[0]);
        assertThat(ss).isNotNull().isNotEmpty().hasSize(1).containsExactly("a");

        final Object[] os = s.toArray();
        assertThat(os).isNotNull().isNotEmpty().hasSize(1).containsExactly("a");
    }

    @Test
    public void testSingletonTIterator() {
        final SortedSet<String> s  = SortedSets.singleton("a");
        final Iterator<String>  si = s.iterator();
        assertThat(si).hasNext();
        assertThat(si.next()).isEqualTo("a");
        assertThat(si).isExhausted();
    }

    @Test
    public void testSingletonTHeadSet() {
        final SortedSet<String> s = SortedSets.singleton("b");
        assertEquals(s, s.headSet("c"));
        assertEquals(SortedSets.empty(), s.headSet("b"));
        assertEquals(SortedSets.empty(), s.headSet("a"));
    }

    @Test
    public void testSingletonTSubSet() {
        final SortedSet<String> s = SortedSets.singleton("b");
        assertEquals(s, s.subSet("a", "c"));
        assertEquals(s, s.subSet("b", "c"));
        assertEquals(SortedSets.empty(), s.subSet("a", "b"));
        assertEquals(SortedSets.empty(), s.subSet("0", "9"));
    }

    @Test
    public void testSingletonTTailSet() {
        final SortedSet<String> s = SortedSets.singleton("b");
        assertThat(s).isNotNull().isNotEmpty();
        assertThat(s.tailSet("a")).isNotNull().isNotEmpty().containsExactlyElementsOf(s);
        assertThat(s.tailSet("b")).isNotNull().isNotEmpty().containsExactlyElementsOf(s);
        assertThat(s.tailSet("c")).isNotNull().isEmpty();
    }

    @Test
    public void testSingletonTAdd() {
        assertThrows(UnsupportedOperationException.class, () -> {
            //noinspection WriteOnlyObject
            SortedSets.singleton("a").add("b");
        });
    }

    @Test
    public void testSingletonTAddAll() {
        assertThrows(UnsupportedOperationException.class, () -> {
            //noinspection RedundantCollectionOperation,WriteOnlyObject
            SortedSets.singleton("a").addAll(Collections.singletonList("b"));
        });
    }

    @Test
    public void testSingletonTClear() {
        assertThrows(UnsupportedOperationException.class, () -> {
            //noinspection WriteOnlyObject
            SortedSets.singleton("a").clear();
        });
    }

    @Test
    public void testSingletonTRemove() {
        assertThrows(UnsupportedOperationException.class, () -> {
            //noinspection WriteOnlyObject
            SortedSets.singleton("a").remove("b");
        });
    }

    @Test
    public void testSingletonTRemoveAll() {
        assertThrows(UnsupportedOperationException.class, () -> {
            //noinspection WriteOnlyObject,SlowAbstractSetRemoveAll
            SortedSets.singleton("a").removeAll(Arrays.asList("a", "b", "c"));
        });
    }

    @Test
    public void testSingletonTRetainAll() {
        assertThrows(UnsupportedOperationException.class, () -> {
            //noinspection WriteOnlyObject
            SortedSets.singleton("a").retainAll(Arrays.asList("a", "b"));
        });
    }
}
