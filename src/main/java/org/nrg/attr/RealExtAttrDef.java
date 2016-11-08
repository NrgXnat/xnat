/*
 * ExtAttr: org.nrg.attr.RealExtAttrDef
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
public class RealExtAttrDef<S> extends NumericExtAttrDef<Double,S> {
    private static final double DEFAULT_SCALE = 1.0;
    private double scale = DEFAULT_SCALE;

    public RealExtAttrDef(final String name, final S attr) {
        super(name, attr);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AbstractExtAttrDef#apply(java.lang.Double)
     */
    public final Iterable<ExtAttrValue> apply(final Double a) throws ExtAttrException {
        return applyDouble(a);
    }
    
    /*
     * (non-Javadoc)
     * @see org.nrg.attr.NumericExtAttrDef#scale(java.lang.Number)
     */
    public Double scale(final Double d) { return scale * d; }

    public RealExtAttrDef<S> setScale(final double s) {
        this.scale = s;
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.NumericExtAttrDef#valueOf(java.lang.String)
     */
    public Double valueOf(final String s) { return Double.valueOf(s); }
}
