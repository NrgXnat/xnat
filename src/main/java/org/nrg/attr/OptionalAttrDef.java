/*
 * ExtAttr: org.nrg.attr.OptionalAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.Collections;
import java.util.Map;

/**
 * OptionalAttrDef is a wrapper for another external attribute definitions
 * that prevents any Exceptions from being thrown by foldl or apply; if
 * any exceptions are thrown from the underlying method, no exception is
 * thrown, but the eventual apply will return empty.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class OptionalAttrDef<S,V,A> extends AbstractExtAttrDef<S,V,A> {
    public static <S,V,A> OptionalAttrDef<S,V,A> wrap(final EvaluableAttrDef<S,V,A> base) {
        return new OptionalAttrDef<S,V,A>(base);
    }
    
    private final EvaluableAttrDef<S,V,A> base;

    private boolean isValid = true;
    
    /**
     * 
     * @param base attribute definition to wrap
     */
    public OptionalAttrDef(final EvaluableAttrDef<S,V,A> base) {
        super(base.getName(), base.getAttrs());
        this.base = base;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.EvaluableAttrDef#apply(java.lang.Object)
     */
    public Iterable<ExtAttrValue> apply(final A a) {
        if (isValid) {
            try {
                return base.apply(a);
            } catch (Throwable skip) {}
        }
        return Collections.<ExtAttrValue>emptyList();
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.EvaluableAttrDef#foldl(java.lang.Object, java.util.Map)
     */
    public A foldl(final A a, final Map<? extends S,? extends V> m) {
        if (isValid) {
            try {
                return base.foldl(a, m);
            } catch (Throwable t) {
                isValid = false;
                return a;
            }
        } else {
            return a;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.Foldable#start()
     */
    public A start() {
        return base.start();
    }
}
