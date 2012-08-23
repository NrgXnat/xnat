/**
 * Copyright (c) 2012 Washington University
 */
package org.nrg.attr;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * @author Kevin A. Archie <karchie@wustl.edu>
 *
 */
public abstract class AbstractExtAttrDef<S,V,A>
implements ExtAttrDef<S>,EvaluableAttrDef<S,V,A> {
    private ImmutableSet<S> attrs;
    private Set<S> optional = Sets.newHashSet();
    private String name;

    protected AbstractExtAttrDef(final String name, final Iterable<S> attrs) {
        this.attrs = ImmutableSet.copyOf(attrs);
        this.name = name;
    }

    protected AbstractExtAttrDef(final String name, final S...attrs) {
        this(name, Arrays.asList(attrs));
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.ExtAttrDef#getAttrs()
     */
    public Set<S> getAttrs() {
        return attrs;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.ExtAttrDef#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * Basic apply implementation for subclasses with A=String
     * @param value content
     * @return ExtAttrValue representing the content
     */
    protected Iterable<ExtAttrValue> applyString(final String value) throws ExtAttrException {
        if (Strings.isNullOrEmpty(value)) {
            throw new NoUniqueValueException(getName());
        } else {
            return Collections.<ExtAttrValue>singleton(new BasicExtAttrValue(name, value));
        }
    }

    /**
     * Basic apply implementation for subclasses with A=Double
     * @param a content
     * @return ExtAttrValue representing the content
     * @throws ExtAttrException
     */
    protected Iterable<ExtAttrValue> applyDouble(final Double a) throws ExtAttrException {
        if (null == a) {
            throw new NoUniqueValueException(this.getName());
        } else {
            return Collections.<ExtAttrValue>singletonList(new BasicExtAttrValue(getName(), String.valueOf(a)));
        }
    }

    /**
     * Basic apply implementation for subclasses with A=Integer
     * @param a content
     * @return ExtAttrValue representing the content
     * @throws ExtAttrException
     */
    protected Iterable<ExtAttrValue> applyInteger(final Integer a) throws ExtAttrException {
        if (null == a) {
            throw new NoUniqueValueException(this.getName());
        } else {
            return Collections.<ExtAttrValue>singletonList(new BasicExtAttrValue(getName(), String.valueOf(a)));
        }
    }


    /*
     * (non-Javadoc)
     * @see org.nrg.attr.EvaluableAttrDef#foldl(java.lang.Iterable)
     */
    public final Iterable<ExtAttrValue> foldl(final Iterable<? extends Map<? extends S,? extends V>> ms)
            throws ExtAttrException {
        A a = start();
        for (final Map<? extends S,? extends V> m : ms) {
            a = foldl(a, m);
        }
        return apply(a);
    }

    public static <S,V> Iterable<ExtAttrValue> foldl(final ExtAttrDef<S> ea, final Iterable<? extends Map<? extends S,? extends V>> ms)
            throws ExtAttrException {
        @SuppressWarnings("unchecked")
        final AbstractExtAttrDef<S,V,?> attr = (AbstractExtAttrDef<S,V,?>)ea;
        return attr.foldl(ms);
    }

    public void makeOptional(final S...attrs) {
        optional.addAll(Arrays.asList(attrs));
    }

    public void makeRequired(final S...attrs) {
        optional.removeAll(Arrays.asList(attrs));
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.ExtAttrDef#requires(java.lang.Object)
     */
    public boolean requires(S attr) {
        return attrs.contains(attr) && !optional.contains(attr);
    }


    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString());
        sb.append(":").append(getName()).append("(");
        Joiner.on(",").appendTo(sb, getAttrs());
        return sb.append(")").toString();
    }
}
