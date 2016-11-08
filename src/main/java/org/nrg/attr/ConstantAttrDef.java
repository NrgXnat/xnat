/*
 * ExtAttr: org.nrg.attr.ConstantAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.Collections;
import java.util.Map;

import com.google.common.base.Objects;

/**
 * ExtAttrDef that always produces a fixed value.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
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

    /**
     * {@inheritDoc}
     */
    public Object start() { return null; }

    /**
     * {@inheritDoc}
     */
    public Object foldl(final Object o, final Map<? extends S,?> m) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Iterable<ExtAttrValue> apply(final Object object) {
        return Collections.singleton(v);
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return Objects.hashCode(getName(), v);
    }

    /**
     * {@inheritDoc}
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
