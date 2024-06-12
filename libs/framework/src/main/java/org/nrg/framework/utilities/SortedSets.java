/*
 * framework: org.nrg.framework.utilities.SortedSets
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Kevin A. Archie
 */
@SuppressWarnings("Duplicates")
public final class SortedSets {
    private static final String IMMUTABLE_MESSAGE = "cannot modify immutable set";
    
    private SortedSets() {}

    /**
     * Creates an immutable, empty SortedSet.
     * @param <T>    The parameterized type of the empty sorted set to create.
     * @return An empty sorted set.
     */
    public static <T> SortedSet<T> empty() {
        return new SortedSet<T>() {
            public Comparator<? super T> comparator() { return null; }

            public T first() {
                throw new NoSuchElementException();
            }

            public SortedSet<T> headSet(T toElement) {
                return this;
            }

            public T last() { 
                throw new NoSuchElementException();
            }

            public SortedSet<T> subSet(T fromElement, T toElement) {
                return this;
            }

            public SortedSet<T> tailSet(T fromElement) {
                return this;
            }

            public boolean add(T e) {
                throw new UnsupportedOperationException(IMMUTABLE_MESSAGE);
            }

            public boolean addAll(Collection<? extends T> c) {
                throw new UnsupportedOperationException(IMMUTABLE_MESSAGE);
            }

            public void clear() {}

            public boolean contains(Object o) {
                return false;
            }

            public boolean containsAll(Collection<?> c) {
                return c.isEmpty();
            }

            public boolean equals(Object o) {
                return o instanceof Set && ((Set<?>)o).isEmpty();
            }

            public int hashCode() { return 0; }

            public boolean isEmpty() { return true; }

            public Iterator<T> iterator() {
                return Collections.<T>emptyList().iterator();
            }

            public boolean remove(Object o) {
                return false;
            }

            public boolean removeAll(Collection<?> c) {
                return false;
            }

            public boolean retainAll(Collection<?> c) {
                return false;
            }

            public int size() { return 0; }

            public Object[] toArray() { return new Object[0]; }

            @SuppressWarnings({"unchecked", "TypeParameterHidesVisibleType"})
            public <T> T[] toArray(T[] a) { return (T[]) Array.newInstance(a.getClass(), 0); }          
        };
    }

    /**
     * Creates a singleton set containing the single object submitted.
     * @param t             The object to store in the set.
     * @param comparator    The comparator used for sorting.
     * @param <T>           The data type of the stored object.
     * @return A sorted set containing the submitted object.
     */
    public static <T extends Comparable<T>> SortedSet<T> singleton(final T t, final Comparator<? super T> comparator) {
        return new SortedSet<T>() {
            public Comparator<? super T> comparator() { return comparator; }

            private int compareTo(T other) {
                if (null == comparator) {
                    return t.compareTo(other);
                } else {
                    return comparator.compare(t, other);
                }
            }

            public T first() { return t; }

            public SortedSet<T> headSet(T toElement) {
                if (compareTo(toElement) < 0) {
                    return this;
                } else {
                    return empty();
                }
            }

            public T last() { return t; }

            public SortedSet<T> subSet(T fromElement, T toElement) {
                if (compareTo(fromElement) >= 0 && compareTo(toElement) < 0) {
                    return this;
                } else {
                    return empty();
                }
            }

            public SortedSet<T> tailSet(T fromElement) {
                if (compareTo(fromElement) >= 0) {
                    return this;
                } else {
                    return empty();
                }
            }

            public boolean add(T e) {
                throw new UnsupportedOperationException(IMMUTABLE_MESSAGE);
            }

            public boolean addAll(Collection<? extends T> c) {
                throw new UnsupportedOperationException(IMMUTABLE_MESSAGE);
            }

            public void clear() {
                throw new UnsupportedOperationException(IMMUTABLE_MESSAGE);
            }

            public boolean contains(Object o) {
                return t.equals(o);
            }

            public boolean containsAll(Collection<?> c) {
                for (Object o : c) {
                    if (!t.equals(o)) {
                        return false;
                    }
                }
                return true;
            }

            public boolean equals(Object o) {
                if (o instanceof Set) {
                    final Set<?> s = (Set<?>)o;
                    return 1 == s.size() && s.contains(t);
                } else {
                    return false;
                }
            }

            public int hashCode() {
                return null == t ? 0 : t.hashCode();
            }

            public boolean isEmpty() { return false; }

            public Iterator<T> iterator() {
                return Collections.singleton(t).iterator();
            }

            public boolean remove(Object o) {
                throw new UnsupportedOperationException(IMMUTABLE_MESSAGE);
            }

            public boolean removeAll(Collection<?> c) {
                throw new UnsupportedOperationException(IMMUTABLE_MESSAGE);
            }

            public boolean retainAll(Collection<?> c) {
                throw new UnsupportedOperationException(IMMUTABLE_MESSAGE);
            }

            public int size() {
                return 1;
            }

            public Object[] toArray() {
                return new Object[]{t};
            }

            @SuppressWarnings({"unchecked", "TypeParameterHidesVisibleType"})
            public <T> T[] toArray(T[] a) {
                final T[] ts = (T[])Array.newInstance(t.getClass(), 1);
                ts[0] = (T)t;
                return ts;
            }
        };
    }

    public static <T extends Comparable<T>> SortedSet<T> singleton(T t) {
        return singleton(t, null);
    }
}
