/**
 * Copyright (c) 2012 Washington University
 */
package org.nrg.attr;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * @author Kevin A. Archie <karchie@wustl.edu>
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
