/*
 * framework: org.nrg.framework.utilities.SortedSetsTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;

import org.junit.Test;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class SortedSetsTest {

    /**
     * Test method for {@link org.nrg.util.SortedSets#empty()}.
     */
    @Test
    public void testEmpty() {
        final SortedSet<Object> s = SortedSets.empty();
        assertTrue(s.isEmpty());
        assertEquals(0, s.size());
        s.clear();  // no-op
        assertFalse(s.contains(null));
    }
    
    public void testEmptyHasNullComparator() {
        final SortedSet<Object> s = SortedSets.empty();
        assertNull(s.comparator());
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testEmptyFirst() {
        final SortedSet<Object> s = SortedSets.empty();
        s.first();
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testEmptyLast() {
        final SortedSet<Object> s = SortedSets.empty();
        s.last();
    }
    
    @Test
    public void testEmptyEquals() {
        final SortedSet<Object> s = SortedSets.empty();
        assertTrue(s.equals(new HashSet<Object>(Arrays.asList())));
        assertFalse(s.equals(new HashSet<Object>(Arrays.asList("a"))));
    }

    @Test
    public void testEmptyHeadSet() {
        final SortedSet<String> s = SortedSets.empty();
        final SortedSet<String> ss = s.headSet("a");
        assertTrue(ss.isEmpty());
    }
    
    @Test
    public void testEmptySubSet() {
        final SortedSet<String> s = SortedSets.empty();
        final SortedSet<String> ss = s.subSet("a", "c");
        assertTrue(ss.isEmpty());
    }
    
    @Test
    public void testEmptyTailSet() {
        final SortedSet<String> s = SortedSets.empty();
        final SortedSet<String> ss = s.tailSet("a");
        assertTrue(ss.isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEmptyAdd() {
        final SortedSet<Object> s = SortedSets.empty();
        s.add("a");
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testEmptyAddAll() {
        final SortedSet<Object> s = SortedSets.empty();
        s.addAll(Arrays.asList("a", "b"));
    }
    
    @Test
    public void testEmptyContains() {
        final SortedSet<Object> s = SortedSets.empty();
        assertFalse(s.contains(null));
        assertFalse(s.contains("a"));
        assertFalse(s.contains(s));
    }
    
    @Test
    public void testEmptyContainsAll() {
        final SortedSet<Object> s = SortedSets.empty();
        assertFalse(s.containsAll(Arrays.asList("a", "b")));
        assertTrue(s.containsAll(Arrays.asList()));
    }
    
    @Test
    public void testEmptyHashCode() {
        assertEquals(0, SortedSets.empty().hashCode());
    }
    
    @Test
    public void testEmptyIterator() {
        assertFalse(SortedSets.empty().iterator().hasNext());
    }
    
    @Test
    public void testEmptyRemove() {
        final SortedSet<Object> s = SortedSets.empty();
        assertFalse(s.remove(null));
        assertFalse(s.remove("a"));
        assertFalse(s.remove(s));
    }
    
    @Test
    public void testEmptyRemoveAll() {
        final SortedSet<Object> s = SortedSets.empty();
        assertFalse(s.removeAll(Arrays.asList()));
        assertFalse(s.removeAll(Arrays.asList("a", "b")));
    }
    
    @Test
    public void testEmptyRetainAll() {
        final SortedSet<Object> s = SortedSets.empty();
        assertFalse(s.retainAll(Arrays.asList()));
        assertFalse(s.retainAll(Arrays.asList("a", "b")));
    }
    
    @Test
    public void testEmptyToArray() {
        final SortedSet<Object> s = SortedSets.empty();
        assertEquals(0, s.toArray().length);
        assertEquals(0, s.toArray(new Object[0]).length);
    }
    
    /**
     * Test method for {@link org.nrg.util.SortedSets#singleton(java.lang.Object, java.util.Comparator)}.
     */
    @Test
    public void testSingletonTComparatorOfQsuperT() {
        // Use (and verify) reverse-alphabetical ordering
        final Comparator<String> backwards = new Comparator<String>() {
            public int compare(final String a, final String b) {
                return b.compareTo(a);
            }
        };
        final SortedSet<String> s = SortedSets.singleton("b", backwards);
        assertEquals(1, s.size());
        assertEquals("b", s.first());
        
        assertEquals(s, s.headSet("a"));
        assertEquals(SortedSets.empty(), s.headSet("b"));
        assertEquals(SortedSets.empty(), s.headSet("c"));
        
        assertEquals(SortedSets.empty(), s.tailSet("a"));
        assertEquals(s, s.tailSet("b"));
        assertEquals(s, s.tailSet("c"));
    }

    /**
     * Test method for {@link org.nrg.util.SortedSets#singleton(java.lang.Object)}.
     */
    @Test
    public void testSingletonT() {
        final SortedSet<String> s = SortedSets.singleton("a");
        assertFalse(s.isEmpty());
        assertNull(s.comparator());
        assertEquals(1, s.size());
        assertEquals("a", s.first());
        assertEquals("a", s.last());
        assertEquals("a".hashCode(), s.hashCode());
        assertEquals(s, new HashSet<String>(Arrays.asList("a")));
        assertTrue(s.contains("a"));
        assertFalse(s.contains("b"));
        assertTrue(s.containsAll(Arrays.asList()));
        assertTrue(s.containsAll(Arrays.asList("a")));
        assertFalse(s.containsAll(Arrays.asList("a", "b")));
        
        final String[] ss = s.toArray(new String[0]);
        assertEquals(1, ss.length);
        assertEquals("a", ss[0]);
        
        final Object[] os = s.toArray();
        assertEquals(1, os.length);
        assertEquals("a", os[0]);
    }
    
    @Test
    public void testSingletonTIterator() {
        final SortedSet<String> s = SortedSets.singleton("a");
        final Iterator<String> si = s.iterator();
        assertTrue(si.hasNext());
        assertEquals("a", si.next());
        assertFalse(si.hasNext());
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
    public void testSingetonTTailSet() {
        final SortedSet<String> s = SortedSets.singleton("b");
        assertEquals(s, s.tailSet("a"));
        assertEquals(s, s.tailSet("b"));
        assertEquals(SortedSets.empty(), s.tailSet("c"));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testSingletonTAdd() {
        SortedSets.singleton("a").add("b");
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testSingletonTAddAll() {
        SortedSets.singleton("a").addAll(Arrays.asList("b"));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testSingletonTClear() {
        SortedSets.singleton("a").clear();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testSingletonTRemove() {
        SortedSets.singleton("a").remove("b");
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testSingletonTRemoveAll() {
        SortedSets.singleton("a").removeAll(Arrays.asList("a", "b", "c"));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testSingletonTRetainAll() {
        SortedSets.singleton("a").retainAll(Arrays.asList("a", "b"));
    }
}
