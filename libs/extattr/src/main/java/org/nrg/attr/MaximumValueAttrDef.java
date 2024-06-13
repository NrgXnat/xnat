/*
 * ExtAttr: org.nrg.attr.MaximumValueAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.Map;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class MaximumValueAttrDef<S,V,A extends Comparable<A>> extends AbstractExtAttrDef<S,V,A> {
    private final EvaluableAttrDef<S,V,A> base;
    
    public MaximumValueAttrDef(final EvaluableAttrDef<S,V,A> base) {
        super(base.getName(), base.getAttrs());
        this.base = base;
    }
    
    public static <S,V,A extends Comparable<A>> MaximumValueAttrDef<S,V,A> wrap(final EvaluableAttrDef<S,V,A> base) {
        return new MaximumValueAttrDef<S,V,A>(base);
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
        final A v = base.foldl(base.start(), m);
        return (null != a &&  a.compareTo(v) > 0) ? a : v;
    }
    
    /*
     * (non-Javadoc)
     * @see org.nrg.attr.EvaluableAttrDef#apply(java.lang.Object)
     */
    public Iterable<ExtAttrValue> apply(final A a) throws ExtAttrException {
        return base.apply(a);
    }
}
