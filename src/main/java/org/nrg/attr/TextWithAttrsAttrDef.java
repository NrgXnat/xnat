/*
 * ExtAttr: org.nrg.attr.TextWithAttrsAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class TextWithAttrsAttrDef<S,V>
extends AbstractExtAttrDef<S,V,Map<S,V>> {
    private final S s;
    private final ImmutableMap<String,S> attrs;
    private boolean ignoreNull;
    private final ImmutableSet<S> optional;
    private final Set<S> invalid = Sets.newHashSet();

    public TextWithAttrsAttrDef(final String name, final S s,
            final Map<String,? extends S> m,
            final boolean ignoreNull,
            final Iterable<S> optional) {
        super(name, Iterables.concat(m.values(),
                null == s ? Collections.<S>emptyList() : Collections.singleton(s)));
        this.s = s;
        this.attrs = ImmutableMap.copyOf(m);
        this.ignoreNull = ignoreNull;
        this.optional = ImmutableSet.copyOf(optional);
    }

    public TextWithAttrsAttrDef(final String name, final S s,
            final String[] names, final S[] attrs,
            final boolean ignoreNull, final Iterable<S> optional) {
        this(name, s, Utils.zipmap(names, attrs), ignoreNull, optional);
    }

    public TextWithAttrsAttrDef(final String name, final S s,
            final String[] names, final S[] attrs,
            final boolean ignoreNull) {
        this(name, s, Utils.zipmap(names, attrs), ignoreNull, Collections.<S>emptyList());
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.ExtAttrDef#apply(java.lang.Object)
     */
    public Iterable<ExtAttrValue> apply(final Map<S,V> a) throws NoUniqueValueException {
        final V v = invalid.contains(s) ? null : a.get(s);
        final ImmutableMap.Builder<String,String> attrbuilder = ImmutableMap.builder();
        for (final Map.Entry<String,S> me : attrs.entrySet()) {
            final S sa = me.getValue();
            if (null != sa && !invalid.contains(sa)) {
                final V va = a.get(sa);
                if (null != va) {
                    attrbuilder.put(me.getKey(), va.toString());
                } else if (!ignoreNull && optional.contains(sa)) {
                    invalid.add(sa);
                } else {
                    throw new NoUniqueValueException(getName());
                }
            }
        }
        return Collections.<ExtAttrValue>singleton(new BasicExtAttrValue(getName(),
                null == v ? null : v.toString(), attrbuilder.build()));
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.ExtAttrDef#fold(java.lang.Object, java.util.Map)
     */
    public Map<S,V> foldl(final Map<S,V> a, final Map<? extends S,? extends V> m)
            throws ExtAttrException {
        if (null != s) {
            foldone(a, s, m.get(s));
        }
        for (final S sa : attrs.values()) {
            foldone(a, sa, m.get(sa));
        }
        return a;
    }

    /**
     * Fold one native attribute in place
     * @param a accumulator
     * @param si native attribute index
     * @param vi native attribute value
     * @throws ExtAttrException
     */
    private void foldone(final Map<S,V> a, final S si, final V vi)
            throws ExtAttrException {
        if (null == vi) {
            if (ignoreNull) {
                // ok, continue
            } else if (optional.contains(si)) {
                invalid.add(si);
                return;
            } else {
                throw new ConversionFailureException(si, null, "attribute as null value");
            }
        }
        final V va = a.get(si);
        if (null != va && !va.equals(vi)) {
            if (optional.contains(si)) {
                invalid.add(si);
                return;
            } else {
                throw new NoUniqueValueException(getName(), new String[] { va.toString(), null == vi ? null : vi.toString() });
            }
        }
        a.put(si, vi);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.ExtAttrDef#start()
     */
    public Map<S,V> start() { return Maps.<S,V>newHashMap(); }

    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString()).append("+");
        Joiner.on(",").appendTo(sb, attrs.entrySet());
        return sb.toString();
    }
}
