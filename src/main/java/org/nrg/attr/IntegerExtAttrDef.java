/*
 * ExtAttr: org.nrg.attr.IntegerExtAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class IntegerExtAttrDef<S> extends NumericExtAttrDef<Integer, S> {
    private static final int DEFAULT_SCALE = 1;
    private int scale = DEFAULT_SCALE;
    
    public IntegerExtAttrDef(final String name, final S attr) {
        super(name, attr);
    }
    
    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AbstractExtAttrDef#apply(java.lang.Integer)
     */
    public Iterable<ExtAttrValue> apply(final Integer a) throws ExtAttrException {
        return applyInteger(a);
    }
    
    /*
     * (non-Javadoc)
     * @see org.nrg.attr.NumericExtAttrDef#scale(java.lang.Number)
     */
    public Integer scale(final Integer d) { return scale * d; }
    
    public IntegerExtAttrDef<S> setScale(final int s) {
        this.scale = s;
        return this;
    }
    
    /*
     * (non-Javadoc)
     * @see org.nrg.attr.NumericExtAttrDef#valueOf(java.lang.String)
     */
    public Integer valueOf(final String s) { return Integer.valueOf(s); }
}
