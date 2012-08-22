/**
 * Copyright (c) 2006-2012 Washington University
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
 * @author Kevin A. Archie <karchie@wustl.edu>
 *
 */
public abstract class AbstractAttrAdapter<S,V> implements AttrAdapter<S,V> {
    private final MutableAttrDefs<S> attrDefs;

    public AbstractAttrAdapter(final MutableAttrDefs<S> ad,
            final AttrDefs<S>...attrs) {
        this.attrDefs = ad;
        add(attrs);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AttrAdapter#add(org.nrg.attr.AttrDefs<S>[])
     */
    public final void add(final AttrDefs<S>...attrs) {
        attrDefs.add(attrs);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AttrAdapter#add(org.nrg.attr.ExtAttrDef<S>[])
     */
    public final void add(final ExtAttrDef<S>...attrs) {
        add(Arrays.asList(attrs));
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AttrAdapter#add(java.lang.Iterable)
     */
    public final void add(final Iterable<? extends ExtAttrDef<S>> attrs) {
        for (final ExtAttrDef<S> a : attrs) {
            attrDefs.add(a);
        }
    }

    protected final MutableAttrDefs<S> getDefs() { return attrDefs; }

    /**
     * Implements {@link AttrAdapter#getMultipleValuesGiven(Map, Map)}
     * @param given
     * @param defs
     * @param failures
     * @return
     * @throws ExtAttrException
     */
    /*
    private final List<Set<ExtAttrValue>>
    getMultipleValuesGiven(final Map<S,V> given, final Iterable<ExtAttrDef<S>> defs,
            final Map<ExtAttrDef<S>,Exception> failures)
            throws ExtAttrException {
        final List<Set<ExtAttrValue>> values = Lists.newArrayList();
        final Map<S,ConversionFailureException> failedS = Maps.newHashMap();

        if (!defs.iterator().hasNext()) {
            throw new IllegalArgumentException("empty attribute definitions");
        }

        // This is now tricky. Some variables return a sequence of ExtAttrValues rather than a single
        // value, so we need to collect return groups rather than individual return values.
        for (final ExtAttrDef<S> ea : defs) {
            final EvaluableAttrDef<S,V,?> eval = (EvaluableAttrDef<S,V,?>)ea;
            final Set<ExtAttrValue> attrVals = Sets.newLinkedHashSet();

            attrVals.addAll(eval.foldl(getUniqueCombinationsGivenValues(given,
                    ea.getAttrs(), failedS)));
            final Set<ExtAttrValue> attrVals = Sets.newHashSet();
            values.add(attrVals);

            // More than one combination of native values might map to a single value
            // of the external attribute. In this case, we want to return the single
            // external attribute value once only. Also, some of the underlying objects
            // may not have all the components necessary to build this external attribute;
            // skip insufficient combinations in hopes of getting a valid one.
            // (This last problem comes up in practice in Philips DICOM: many series contain
            // secondary objects that are missing Patient, Study, or Series-level fields.)
            attrVals.add(e)
            VALUES: for (final Map<S,V> value: getUniqueCombinationsGivenValues(given, ea.getAttrs(), failedS)) {
                for (final S attr : ea.getAttrs()) {
                    if (!value.containsKey(attr) && ea.requires(attr)) {
                        continue VALUES; // can't build the attribute from this value
                    }
                }
                try {
                    attrVals.add(eval.foldl)
                    attrVals.add(ea.ea.convert(value));
                } catch (ConversionFailureException e) {
                    failures.put(ea, e);
                }
            }

            if (attrVals.isEmpty()) {
                // Constant attributes don't need native attributes.
                if (ea instanceof ExtAttrDefString.Constant<?,?>) {
                    try {
                        attrVals.add(ea.convert(null));
                    } catch (ConversionFailureException e) {
                        throw new RuntimeException(e);    // can't happen
                    }
                } else if (ea instanceof Optional) {
                    // Don't generate a failure if the attribute is optional
                } else {
                    failures.put(ea, new ConversionFailureException(ea, null, "no native values available"));
                }
            }
        }

        return values;
    }

    private final List<Set<ExtAttrValue>>
    getMultipleValuesGiven(final Map<S,V> given, final Iterable<ExtAttrDef<S>> defs,
            final Map<ExtAttrDef<S>,Exception> failures) throws ExtAttrException {
        throw new UnsupportedOperationException();
    }
     */

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AttrAdapter#getMultipleValuesGiven(java.util.Map, java.util.Map)
     */
    /*
    public final List<Set<ExtAttrValue>>
    getMultipleValuesGiven(final Map<S,V> given, final Map<ExtAttrDef<S>,Exception> failures)
    throws ExtAttrException {
    	throw new UnsupportedOperationException();
        return getMultipleValuesGiven(given, attrDefs, failures);
    }
     */

    protected abstract Collection<Map<S,V>>
    getUniqueCombinationsGivenValues(Map<S,V> given, Collection<S> attrs, Map<S,ConversionFailureException> failed);
    
    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AttrAdapter#getValues(java.util.Map)
     */
    public final List<ExtAttrValue> getValues(final Map<ExtAttrDef<S>,Throwable> failed)
            throws ExtAttrException {
        return getValuesGiven(Collections.<S,V>emptyMap(), failed);
    }


    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AttrAdapter#getValuesGiven(java.util.Map, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public final List<ExtAttrValue> getValuesGiven(final Map<S,V> given,
            final Map<ExtAttrDef<S>,Throwable> failed) {
        final Map<S,ConversionFailureException> failures = Maps.newLinkedHashMap();
        final List<ExtAttrValue> values = Lists.newArrayList();
        for (final ExtAttrDef<S> ea : attrDefs) {
            try {
            Iterables.addAll(values,
                    ((EvaluableAttrDef<S,V,?>)ea).foldl(getUniqueCombinationsGivenValues(given, ea.getAttrs(), failures)));
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
    public final int remove(final S...nativeAttrs) {
        int removed = 0;
        for (final S attr : nativeAttrs) {
            removed += attrDefs.remove(attr);
        }
        return removed;
    }


    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AttrAdapter#remove(java.lang.String[])
     */
    public final int remove(final String...attrNames) {
        int removed = 0;
        for (final String name : attrNames) {
            removed += attrDefs.remove(name);
        }
        return removed;
    }
}
