/*
 * ExtAttr: org.nrg.attr.ValueJoiningAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Wrapper attribute that joins the text values of the contained attribute,
 * using the provided separator.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class ValueJoiningAttrDef<S,V,A> extends AbstractExtAttrDef<S,V,Set<A>> {
    public static <S,V,A> ValueJoiningAttrDef<S,V,A>
    wrap(final EvaluableAttrDef<S,V,A> base, final String separator) {
        return new ValueJoiningAttrDef<S,V,A>(base, separator);
    }

    private final EvaluableAttrDef<S,V,A> base;
    private final Joiner joiner;

    public ValueJoiningAttrDef(final EvaluableAttrDef<S,V,A> base, final String separator) {
        super(base.getName(), base.getAttrs());
        this.base = base;
        joiner = Joiner.on(separator);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.EvaluableAttrDef#apply(java.lang.Object)
     */
    public Iterable<ExtAttrValue> apply(final Set<A> as) throws ExtAttrException {
        final Set<String> ss = Sets.newLinkedHashSet();
        final Map<String,String> attrs = Maps.newLinkedHashMap();
        for (final A a : as) {
            for (final ExtAttrValue v : base.apply(a)) {
                ss.add(v.getText());
                attrs.putAll(v.getAttrs());
            }
        }
        return Collections.<ExtAttrValue>singletonList(new BasicExtAttrValue(base.getName(), joiner.join(ss), attrs));
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.EvaluableAttrDef#foldl(java.lang.Object, java.util.Map)
     */
    public Set<A> foldl(final Set<A> a, final Map<? extends S,? extends V> m)
            throws ExtAttrException {
        final A va = base.foldl(base.start(), m);
        if (null != va) {
            a.add(va);
        }
        return a;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.Foldable#start()
     */
    public Set<A> start() { return Sets.newLinkedHashSet(); }
}
