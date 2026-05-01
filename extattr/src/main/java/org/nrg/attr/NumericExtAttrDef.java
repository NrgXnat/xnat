/*
 * ExtAttr: org.nrg.attr.NumericExtAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public abstract class NumericExtAttrDef<N extends Number,S> extends AbstractExtAttrDef<S,Object,N> {
    private final S attr;

     @SuppressWarnings("unchecked")
    public NumericExtAttrDef(final String name, final S attr) {
        super(name, attr);
        this.attr = attr;
    }
    
    public abstract N scale(N n);
    
    public abstract N valueOf(String s);
    
    /*
     * (non-Javadoc)
     * @see org.nrg.attr.EvaluableAttrDef#foldl(java.lang.Object, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public N foldl(final N a, final Map<? extends S,? extends Object> m)
    throws ExtAttrException {
        final Object ov = m.get(attr);
        if (null == ov) {
            return a;
        } else {
            final N n;
            try {
                n = valueOf(ov.toString());
            } catch (NumberFormatException e) {
                throw new ConversionFailureException(this, ov, "unable to convert", e);
            }
            if (null == a || a.equals(n)) {
                return n;
            } else {
                throw new NoUniqueValueException(this.getName(), Arrays.asList(a, n));
            }
        }
    }
     /*
     * (non-Javadoc)
     * @see org.nrg.attr.Foldable#start()
     */
    public N start() { return null; }
}
