/*
 * ExtAttr: org.nrg.attr.KeyValueAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.Collections;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class KeyValueAttrDef<S,V,A> extends AbstractExtAttrDef<S,V,A> {
    private final EvaluableAttrDef<S,V,A> base;
    
    public KeyValueAttrDef(final String name, final EvaluableAttrDef<S,V,A> base) {
        super(name, base.getAttrs());
        this.base = base;
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
        return Iterables.transform(base.apply(a),
                new Function<ExtAttrValue,ExtAttrValue>() {
            public ExtAttrValue apply(final ExtAttrValue v) {
                return new BasicExtAttrValue(getName(), v.getText(),
                        Collections.singletonMap("name", v.getName()));
            }
        });
    }
    
    public static <S,V,A> KeyValueAttrDef<S,V,A> wrap(final String name, final EvaluableAttrDef<S,V,A> base) {
        return new KeyValueAttrDef<S,V,A>(name, base);
    }
    
    public static <S> KeyValueAttrDef<S,?,?> wrap(final String name, final ExtAttrDef<S> base) {
        return new KeyValueAttrDef<S,Object,Object>(name, (EvaluableAttrDef<S,Object,Object>)base);
    }
}
