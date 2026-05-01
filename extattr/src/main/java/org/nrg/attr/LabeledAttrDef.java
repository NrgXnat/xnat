/*
 * ExtAttr: org.nrg.attr.LabeledAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class LabeledAttrDef<S,V,A>
extends AbstractExtAttrDef<S,V,A> {
    private final EvaluableAttrDef<S,V,A> base;
    private final ImmutableMap<String,String> labels;

    public LabeledAttrDef(final EvaluableAttrDef<S,V,A> base, final Map<String,String> labels) {
        super(base.getName(), base.getAttrs());
        this.base = base;
        final ImmutableMap.Builder<String,String> lb = ImmutableMap.builder();
        lb.putAll(labels);
        this.labels = lb.build();
    }

    public static <S,V,A> LabeledAttrDef<S,V,A>
    create(final EvaluableAttrDef<S,V,A> base, final Map<String,String> labels) {
        return new LabeledAttrDef<S,V,A>(base, labels);
    }

    public static <S,V,A> LabeledAttrDef<S,V,A>
    create(final EvaluableAttrDef<S,V,A> base, final String[] names, final String[] values) {
        return create(base, Utils.zipmap(names, values));
    }

    public static <S,V,A> LabeledAttrDef<S,V,A>
    create(final EvaluableAttrDef<S,V,A> base, final String name, final String value) {
        return create(base, Collections.singletonMap(name, value));
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.ExtAttrDef#start()
     */
    public A start() { return base.start(); }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.ExtAttrDef#fold(java.lang.Object, java.util.Map)
     */
    public A foldl(final A a, final Map<? extends S,? extends V> m)
            throws ExtAttrException {
        return base.foldl(a, m);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.ExtAttrDef#eval(java.lang.Object)
     */
    public Iterable<ExtAttrValue> apply(final A a)
            throws ExtAttrException {
        final List<ExtAttrValue> out = Lists.newArrayList();
        for (final ExtAttrValue v : base.apply(a)) {
            out.add(new BasicExtAttrValue(v.getName(), v.getText(),
                    ImmutableMap.<String,String>builder().putAll(labels).putAll(v.getAttrs()).build()));
        }
        return out;
    }
}
