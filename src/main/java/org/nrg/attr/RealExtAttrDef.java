/**
 * Copyright (c) 2012 Washington University
 */
package org.nrg.attr;

/**
 * @author Kevin A. Archie <karchie@wustl.edu>
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
    @Override
    public final Iterable<ExtAttrValue> apply(final Double a) throws ExtAttrException {
        return super.apply(a);
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
