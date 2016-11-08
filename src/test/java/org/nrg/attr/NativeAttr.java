/*
 * ExtAttr: org.nrg.attr.NativeAttr
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Stub native attribute type used for unit tests
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
@SuppressWarnings("unchecked")
public class NativeAttr implements Comparable<NativeAttr> {
    private static final Map<NativeAttr,Map<NativeAttr,Integer>> comparisons = Maps.newHashMap();

    private final String name;

    private NativeAttr(final String name) {
        this.name = name;
    }

    public final String getName() { 
        return name; 
    }

    @Override
    public final String toString() {
        return name;
    }

    public int compareTo(NativeAttr other) {
        return comparisons.get(this).get(other);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof NativeAttr && 0 == comparisons.get(this).get(o);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public static final NativeAttr A = new NativeAttr("NativeAttr A");
    public static final NativeAttr B = new NativeAttr("NativeAttr B");
    public static final NativeAttr C = new NativeAttr("NativeAttr C");
    public static final NativeAttr D = new NativeAttr("NativeAttr D");

    public interface RWAttrDefSet<S,V> extends AttrDefs<S> {
        public RWAttrDefSet<S,V> addExtAttrDef(ExtAttrDef<S> ead);
    }

    public static final class SampleAttrDefSet<S,V> implements AttrDefs<S> {
        private Set<S> nas = Sets.newHashSet();
        private Map<String,ExtAttrDef<S>> defs = Maps.newLinkedHashMap();

        public SampleAttrDefSet(ExtAttrDef<S>...eads) {
            for (final ExtAttrDef<S> ead : eads)
                addExtAttrDef(ead);
        }

        public Set<S> getNativeAttrs() {
            return new HashSet<S>(nas);
        }

        public ExtAttrDef<S> getExtAttrDef(final String name) {
            return defs.get(name);
        }

        public Iterator<ExtAttrDef<S>> iterator() {
            return Iterators.unmodifiableIterator(defs.values().iterator());
        }

        public SampleAttrDefSet<S,V> addExtAttrDef(final ExtAttrDef<S> ead) {
            defs.put(ead.getName(), ead);
            nas.addAll(ead.getAttrs());
            return this;
        }
    }

    public static final ExtAttrDef<NativeAttr> fextA = new SingleValueTextAttr<NativeAttr>("ext-A", NativeAttr.A);
    public static final ExtAttrDef<NativeAttr> fextC_BA =
        new TextWithAttrsAttrDef<NativeAttr,String>("ext-C", NativeAttr.C,
                ImmutableMap.of("B", NativeAttr.B, "A", NativeAttr.A), true,
                Collections.<NativeAttr>emptySet());

    public static final SampleAttrDefSet<NativeAttr,Float> frads = new SampleAttrDefSet<NativeAttr,Float>(fextA, fextC_BA);

    public static final SampleAttrDefSet<NativeAttr,Float> emptyFrads = new SampleAttrDefSet<NativeAttr,Float>();

    static {
        final Map<NativeAttr,Integer> compareA = Maps.newHashMap();
        compareA.put(A, 0);
        compareA.put(B, -1);
        compareA.put(C, -2);
        compareA.put(D, -3);
        comparisons.put(A, compareA);
        final Map<NativeAttr,Integer> compareB = Maps.newHashMap();
        compareB.put(A, 1);
        compareB.put(B, 0);
        compareB.put(C, -1);
        compareB.put(D, -2);
        comparisons.put(B, compareB);
        final Map<NativeAttr,Integer> compareC = Maps.newHashMap();
        compareC.put(A, 2);
        compareC.put(B, 1);
        compareC.put(C, 0);
        compareC.put(D, 1);
        comparisons.put(C, compareC);
        final Map<NativeAttr,Integer> compareD = Maps.newHashMap();
        compareD.put(A, 3);
        compareD.put(B, 2);
        compareD.put(C, 1);
        compareD.put(D, 0);
        comparisons.put(D, compareD);
    }
}
