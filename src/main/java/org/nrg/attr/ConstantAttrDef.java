/**
 * Copyright (c) 2012 Washington University
 */
package org.nrg.attr;

import java.util.Collections;
import java.util.Map;

import com.google.common.base.Objects;

/**
 * ExtAttrDef that always produces a fixed value.
 * @author Kevin A. Archie <karchie@wustl.edu>
 *
 */
public class ConstantAttrDef<S>
extends AbstractExtAttrDef<S,Object,Object> {
    private final ExtAttrValue v;

    /**
     * Create an ExtAttrDef that always returns the provided value.
     * @param v return value
     */
    @SuppressWarnings("unchecked")
    public ConstantAttrDef(final ExtAttrValue v) {
        super(v.getName());
        this.v = v;
    }

    /**
     * Create an ExtAttrDef with the given name, returning the given
     * value.
     * @param name attribute name
     * @param value attribute value
     */
    public ConstantAttrDef(final String name, final String value) {
        this(new BasicExtAttrValue(name, value));
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.ExtAttrDef#start()
     */
    public Object start() { return null; }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.ExtAttrDef#fold(java.lang.Object, java.util.Map)
     */
    public Object foldl(final Object o, final Map<? extends S,? extends Object> m) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.ExtAttrDef#eval(java.lang.Object)
     */
    public Iterable<ExtAttrValue> apply(final Object _) {
        return Collections.singleton(v);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return Objects.hashCode(getName(), v);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(final Object o) {
        if (o instanceof ConstantAttrDef) {
            final ConstantAttrDef<?> oav = (ConstantAttrDef<?>)o;
            return getName().equals(oav.getName()) && v.equals(oav.v);
        } else {
            return false;
        }
    }
}
