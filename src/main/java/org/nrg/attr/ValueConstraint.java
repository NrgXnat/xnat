/**
 * Copyright (c) 2008 Washington University
 */
package org.nrg.attr;

import java.util.Map;

/**
 * @author Kevin A. Archie <karchie@wustl.edu>
 *
 */
public class ValueConstraint<S,V> implements Map.Entry<S,V>{
    private final S s;
    private final V v;

    public ValueConstraint(final S attribute, final V value) {
        this.s = attribute;
        this.v = value;
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map.Entry#getKey()
     */
    public S getKey() { return s; }

    /*
     * (non-Javadoc)
     * @see java.util.Map.Entry#getValue()
     */
    public V getValue() { return v; }

    /*
     * (non-Javadoc)
     * @see java.util.Map.Entry#setValue(java.lang.Object)
     */
    public V setValue(V v) { throw new UnsupportedOperationException(); }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public final boolean equals(final Object o) {
        if (!(o instanceof ValueConstraint<?,?>)) { return false; }
        final ValueConstraint<?,?> vc = (ValueConstraint<?,?>)o;
        return s.equals(vc.s) && (v == vc.v || (null != v && v.equals(vc.v)));
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public final int hashCode() {
        int result = 17;
        if (null != s) { result = 37 * result + s.hashCode(); }
        if (null != v) { result = 37 * result + v.hashCode(); }
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(s);
        sb.append("->");
        sb.append(v);
        return sb.toString();
    }
}
