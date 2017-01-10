/*
 * ExtAttr: org.nrg.attr.MutableAttrDefs
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Iterators;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * Describes a group of external attributes and their conversions from native fields
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class MutableAttrDefs<S> implements AttrDefs<S> {
    final private Multimap<String, ExtAttrDef<S>> extAttrs = LinkedHashMultimap.create();
    final private Set<S> nativeAttrs;

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public MutableAttrDefs() {
        this(null);
    }

    @SafeVarargs
    public MutableAttrDefs(final AttrDefs<S> base, final ExtAttrDef<S>... adds) {
        this(null, base, adds);
    }

    public MutableAttrDefs(final Comparator<S> comparator) {
        nativeAttrs = new TreeSet<>(comparator);
    }

    @SuppressWarnings("unchecked")
    public MutableAttrDefs(final Comparator<S> comparator,
                           final AttrDefs<S> base, final ExtAttrDef<S>... adds) {
        this(comparator);
        this.add(base);
        this.addAll(Arrays.asList(adds));
    }

    /**
     * Copies all the attributes from another set to this one.
     * There should be no overlapping attribute definitions.
     *
     * @param others AttrDefSet from which attributes are to be copied.
     *
     * @return This collection of mutable attribute definitions.
     */
    @SafeVarargs
    public final MutableAttrDefs<S> add(AttrDefs<S>... others) {
        for (final AttrDefs<S> other : others) {
            for (final ExtAttrDef<S> ea : other) {
                add(ea);
            }

            // All of the native attributes from the old set should
            // have been implicitly transferred.
            for (final S attr : other.getNativeAttrs()) {
                assert nativeAttrs.contains(attr);
            }
        }
        return this;
    }

    /**
     * Adds a new external attribute to this set.
     *
     * @param a external attribute specification
     *
     * @return The newly added external attribute.
     */
    public ExtAttrDef<S> add(ExtAttrDef<S> a) {
        synchronized (this) {
            extAttrs.put(a.getName(), a);
            nativeAttrs.addAll(a.getAttrs());
        }
        return a;
    }

    /**
     * Defines a new placeholder external attribute:
     * no value is assigned from the native information
     *
     * @param name name of the external attribute
     *
     * @return The newly added external attribute.
     */
    public ExtAttrDef<S> add(String name) {
        return add(new ConstantAttrDef<S>(name, null));
    }

    /**
     * Defines a new external attribute, using the default (null) converter
     *
     * @param name name of the external attribute
     * @param attr identifier of the native attribute
     *
     * @return The newly added external attribute.
     */
    public ExtAttrDef<S> add(String name, S attr) {
        return add(new SingleValueTextAttr<>(name, attr));
    }

    /**
     * Defines a new external attribute with a fixed value
     *
     * @param name  The name of the new external attribute.
     * @param value The value of the new external attribute.
     *
     * @return The newly added external attribute.
     */
    public ExtAttrDef<S> add(final String name, final String value) {
        return add(new ConstantAttrDef<S>(name, value));
    }

    /**
     * Adds multiple external attributes to this set.
     *
     * @param extAttrDefs Collection of external attribute specifications
     *
     * @return This collection of mutable attribute definitions.
     */
    public MutableAttrDefs<S> addAll(Collection<? extends ExtAttrDef<S>> extAttrDefs) {
        for (final ExtAttrDef<S> a : extAttrDefs) {
            add(a);
        }
        return this;
    }

    /**
     * Gets the tag values for all native attributes used in this set.
     *
     * @return Set of native tag values
     */
    public Set<S> getNativeAttrs() {
        return Collections.unmodifiableSet(nativeAttrs);
    }

    /**
     * Specifies an iteration over the attribute definitions in the
     * same order they were defined.
     */
    public Iterator<ExtAttrDef<S>> iterator() {
        return Iterators.unmodifiableIterator(extAttrs.values().iterator());
    }

    /**
     * Removes any external attributes using the indicated native attribute from this set.
     *
     * @param attr native attribute to be removed
     *
     * @return The number of external attributes removed
     */
    public int remove(final S attr) {
        int count = 0;

        synchronized (this) {
            for (final Iterator<Map.Entry<String, ExtAttrDef<S>>> i = extAttrs.entries().iterator(); i.hasNext(); ) {
                final Map.Entry<String, ExtAttrDef<S>> e = i.next();
                if (e.getValue().getAttrs().contains(attr)) {
                    i.remove();
                    count++;
                }
            }

            // Rebuild the native attributes set
            nativeAttrs.clear();
            for (final ExtAttrDef<S> ea : this) {
                nativeAttrs.addAll(ea.getAttrs());
            }
        }

        return count;
    }

    /**
     * Removes the named attribute from this set.
     *
     * @param name The name of the external attribute
     *
     * @return The number of attributes removed (1 if present, 0 otherwise)
     */
    public int remove(String name) {
        return (null == extAttrs.removeAll(name)) ? 0 : 1;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString());
        sb.append(LINE_SEPARATOR);
        for (final ExtAttrDef<S> ead : extAttrs.values()) {
            sb.append("  ").append(ead).append(LINE_SEPARATOR);
        }
        return sb.toString();
    }
}
