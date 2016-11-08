/*
 * ExtAttr: org.nrg.attr.SingleValueTextAttr
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
 * Simple attribute that returns a single text value if present, or
 * throws ConversionFailureException if multiple values are present.
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class SingleValueTextAttr<S>
        extends AbstractExtAttrDef<S, Object, String> {
    private final S       attr;
    private final boolean allowNull;
    private final String  format;

    /**
     * Creates an ExtAttrDef for simple, unique text values, ignoring null
     * values of the native attribute.
     *
     * @param name external attribute name
     * @param attr native attribute index
     */
    public SingleValueTextAttr(final String name, final S attr) {
        this(name, attr, true);
    }

    /**
     * Creates an ExtAttrDef for simple, unique text values.
     *
     * @param name      external attribute name
     * @param attr      native attribute index
     * @param allowNull if true, null native attribute values are quietly ignored; if
     *                  false, a null value causes a ConversionFailureException, even
     *                  if a non-null value is already in the accumulator.
     */
    public SingleValueTextAttr(final String name, final S attr, final boolean allowNull) {
        this(name, attr, allowNull, "%s");
    }

    /**
     * Creates an ExtAttrDef for simple, unique text values.
     *
     * @param name      The external attribute name
     * @param attr      The native attribute index
     * @param allowNull If true, null native attribute values are quietly ignored; if false, a null value causes a
     *                  ConversionFailureException, even if a non-null value is already in the accumulator.
     * @param format    The format specified for the attribute value.
     */
    public SingleValueTextAttr(final String name, final S attr, final boolean allowNull, final String format) {
        super(name, Collections.singleton(attr));
        this.attr = attr;
        this.allowNull = allowNull;
        this.format = format;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AbstractExtAttrDef#apply(java.lang.String)
     */
    public final Iterable<ExtAttrValue> apply(final String a) throws ExtAttrException {
        return applyString(a);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(final Object o) {
        if (o instanceof SingleValueTextAttr) {
            final SingleValueTextAttr<?> oa = (SingleValueTextAttr<?>) o;
            return getName().equals(oa.getName()) && attr.equals(oa.attr);
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.ExtAttrDef#fold(java.lang.Object, java.util.Map)
     */
    public String foldl(final String a, final Map<? extends S, ?> m)
            throws NoUniqueValueException {
        final Object vo = m.get(attr);
        if (null == vo && !allowNull) {
            throw new NoUniqueValueException(getName());
        }
        final String vs = null == vo ? null : String.format(format, vo);
        if (null != a && !a.equals(vs)) {
            throw new NoUniqueValueException(getName(), new String[]{a, vs});
        } else {
            return vs;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return Objects.hashCode(getName(), attr);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.ExtAttrDef#start()
     */
    public String start() {
        return null;
    }
}
