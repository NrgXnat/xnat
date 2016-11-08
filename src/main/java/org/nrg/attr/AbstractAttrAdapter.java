/*
 * ExtAttr: org.nrg.attr.AbstractAttrAdapter
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Organizes conversion from native attributes (those found in
 * a data file format, such as DICOM or ECAT), with attribute index
 * type S and value type V, to external attributes with type
 * ExtAttrValue.
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public abstract class AbstractAttrAdapter<S, V> implements AttrAdapter<S, V> {
    private final MutableAttrDefs<S> _attrDefs;

    public AbstractAttrAdapter(final MutableAttrDefs<S> attrDefs,
                               final AttrDefs<S>... attrs) {
        _attrDefs = attrDefs;
        add(attrs);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AttrAdapter#add(org.nrg.attr.AttrDefs<S>[])
     */
    public final void add(final AttrDefs<S>... attrs) {
        _attrDefs.add(attrs);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AttrAdapter#add(org.nrg.attr.ExtAttrDef<S>[])
     */
    public final void add(final ExtAttrDef<S>... attrs) {
        add(Arrays.asList(attrs));
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AttrAdapter#add(java.lang.Iterable)
     */
    public final void add(final Iterable<? extends ExtAttrDef<S>> attrs) {
        for (final ExtAttrDef<S> a : attrs) {
            _attrDefs.add(a);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AttrAdapter#getValues(java.util.Map)
     */
    public final List<ExtAttrValue> getValues(final Map<ExtAttrDef<S>, Throwable> failed)
            throws ExtAttrException {
        return getValuesGiven(Collections.<S, V>emptyMap(), failed);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AttrAdapter#getValuesGiven(java.util.Map, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public final List<ExtAttrValue> getValuesGiven(final Map<S, V> given,
                                                   final Map<ExtAttrDef<S>, Throwable> failed) {
        final Map<S, ConversionFailureException> failures = Maps.newLinkedHashMap();
        final List<ExtAttrValue> values = Lists.newArrayList();
        for (final ExtAttrDef<S> ea : _attrDefs) {
            try {
                Iterables.addAll(values,
                                 ((EvaluableAttrDef<S, V, ?>) ea).foldl(getUniqueCombinationsGivenValues(given, ea.getAttrs(), failures)));
            } catch (Throwable t) {
                failed.put(ea, t);
            }
        }
        return values;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AttrAdapter#remove(S[])
     */
    public final int remove(final S... nativeAttrs) {
        int removed = 0;
        for (final S attr : nativeAttrs) {
            removed += _attrDefs.remove(attr);
        }
        return removed;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AttrAdapter#remove(java.lang.String[])
     */
    public final int remove(final String... attrNames) {
        int removed = 0;
        for (final String name : attrNames) {
            removed += _attrDefs.remove(name);
        }
        return removed;
    }

    protected abstract Collection<Map<S, V>>
    getUniqueCombinationsGivenValues(Map<S, V> given, Collection<S> attrs, Map<S, ConversionFailureException> failed);

    protected final MutableAttrDefs<S> getDefs() {
        return _attrDefs;
    }

}
