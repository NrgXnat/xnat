/*
 * ExtAttr: org.nrg.attr.TransformingExtAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class TransformingExtAttrDef<S,V,A> extends AbstractExtAttrDef<S,V,A> {
    private final EvaluableAttrDef<S,V,A> base;
    private final Function<ExtAttrValue,ExtAttrValue> f;

    public TransformingExtAttrDef(final EvaluableAttrDef<S,V,A> base, Function<ExtAttrValue,ExtAttrValue> f) {
        super(base.getName(), base.getAttrs());
        this.base = base;
        this.f = f;
    }

    public static <S,V,A> TransformingExtAttrDef<S,V,A>
    wrap(final EvaluableAttrDef<S,V,A> base, final Function<ExtAttrValue,ExtAttrValue> f) {
        return new TransformingExtAttrDef<S,V,A>(base, f);
    }

    public static <S,V,A> TransformingExtAttrDef<S,V,A>
    wrapTextFunction(final EvaluableAttrDef<S,V,A> base, final Function<String,String> f) {
        return new TransformingExtAttrDef<S,V,A>(base, new Function<ExtAttrValue,ExtAttrValue>() {
            public ExtAttrValue apply(final ExtAttrValue v) {
                return new BasicExtAttrValue(v.getName(), f.apply(v.getText()), v.getAttrs());
            }
        });
    }
    
    public static <S,V,A> TransformingExtAttrDef<S,V,A>
    wrapTextMap(final EvaluableAttrDef<S,V,A> base, final Map<String,String> m) {
        return wrapTextFunction(base, new Function<String,String>() {
            public String apply(final String k) {
                return m.containsKey(k) ? m.get(k) : k;
            }
        });
    }
    
    public static <S,V,A> TransformingExtAttrDef<S,V,A>
    wrap(final EvaluableAttrDef<S,V,A> base, final String k, final String v) {
        return wrapTextFunction(base, new Function<String,String>() {
            public String apply(final String k_) {
                return Objects.equal(k_, k) ? v : k_;
            }
        });
    }
    
    public static <S,V,A> TransformingExtAttrDef<S,V,A>
    wrap(final EvaluableAttrDef<S,V,A> base,
            final String k1, final String v1,
            final String k2, final String v2) {
        return wrapTextMap(base, ImmutableMap.of(k1, v1, k2, v2));
    }

    public static <S,V,A> TransformingExtAttrDef<S,V,A>
    wrap(final EvaluableAttrDef<S,V,A> base,
            final String k1, final String v1,
            final String k2, final String v2,
            final String k3, final String v3) {
        return wrapTextMap(base, ImmutableMap.of(k1, v1, k2, v2, k3, v3));
    }

    public static <S,V,A> TransformingExtAttrDef<S,V,A>
    wrap(final EvaluableAttrDef<S,V,A> base,
            final String k1, final String v1,
            final String k2, final String v2,
            final String k3, final String v3,
            final String k4, final String v4) {
        return wrapTextMap(base, ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4));
    }

    public static <S,V,A> TransformingExtAttrDef<S,V,A>
    wrap(final EvaluableAttrDef<S,V,A> base,
            final String k1, final String v1,
            final String k2, final String v2,
            final String k3, final String v3,
            final String k4, final String v4,
            final String k5, final String v5) {
        return wrapTextMap(base, ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5));
    }


    /*
     * (non-Javadoc)
     * @see org.nrg.attr.Foldable#start()
     */
    public A start() { return base.start(); }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.EvaluableAttrDef#foldl(java.lang.Object, java.util.Map)
     */
    public A foldl(final A a, final Map<? extends S,? extends V> m) throws ExtAttrException {
        return base.foldl(a, m);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.EvaluableAttrDef#apply(java.lang.Object)
     */
    public Iterable<ExtAttrValue> apply(final A a) throws ExtAttrException {
        return Iterables.transform(base.apply(a), f);
    }
}
