/**
 * Copyright (c) 2006-2010 Washington University
 */
package org.nrg.attr;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;


/**
 * Describes a group of external attributes and their conversions from native fields
 * @author Kevin A. Archie <karchie@wustl.edu>
 *
 */
public class MutableAttrDefs<S,V> implements AttrDefs<S,V> {
	private final Map<String,ExtAttrDef<S,V>> extAttrs = Maps.newLinkedHashMap();
	final private Set<S> nativeAttrs;

	public MutableAttrDefs(final Comparator<S> comparator) {
		nativeAttrs = new TreeSet<S>(comparator);
	}

	public MutableAttrDefs() {
		this(null);
	}

	@SuppressWarnings("unchecked")
	public MutableAttrDefs(final Comparator<S> comparator,
			final AttrDefs<S,V> base, final ExtAttrDef<S,V>...adds) {
		this(comparator);
		this.add(base);
		this.addAll(Arrays.asList(adds));
	}

	public MutableAttrDefs(final AttrDefs<S,V> base, final ExtAttrDef<S,V>...adds) {
		this(null, base, adds);
	}

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final StringBuilder sb = new StringBuilder(super.toString());
		sb.append(LINE_SEPARATOR);
		for (final ExtAttrDef<S,V> ead : extAttrs.values()) {
			sb.append("  ").append(ead).append(LINE_SEPARATOR);
		}
		return sb.toString();
	}

	/**
	 * Adds a new external attribute to this set.
	 * @param a external attribute specification
	 */
	public ExtAttrDef<S,V> add(ExtAttrDef<S,V> a) {
		synchronized (this) {
			final String name = a.getName();
			if (extAttrs.containsKey(name)) {
				throw new IllegalArgumentException("Attribute " + name + " already defined");
			}
			extAttrs.put(name, a);
			nativeAttrs.addAll(a.getAttrs());
		}
		return a;
	}

	/**
	 * Adds multiple external attributes to this set.
	 * @param Collection of external attribute specifications
	 */
	public MutableAttrDefs<S,V> addAll(Iterable<? extends ExtAttrDef<S,V>> as) {
		for (final ExtAttrDef<S,V> a : as) {
			this.add(a);
		}
		return this;
	}

	/**
	 * Defines a new placeholder external attribute:
	 * no value is assigned from the native information
	 * @param name name of the external attribute
	 */
	public ExtAttrDef<S,V> add(String name) {
		return add(new ExtAttrDef.Empty<S,V>(name));
	}

	/**
	 * Defines a new external attribute with a fixed value
	 * @param name
	 * @param value 
	 */
	public ExtAttrDef<S,V> add(final String name, final String value) {
		return add(new ExtAttrDef.Constant<S,V>(name, value));
	}

	/**
	 * Defines a new external attribute, using the default (null) converter
	 * @param name name of the external attribute
	 * @param attr identifier of the native attribute
	 */
	public ExtAttrDef<S,V> add(String name, S attr) {
		return add(new ExtAttrDef.Text<S,V>(name, attr));
	}

	/**
	 * Copies all the attributes from another set to this one.
	 * @param other AttrDefSet from which attributes are to be copied.
	 */
	public MutableAttrDefs<S,V> add(AttrDefs<S,V>...others) {
		for (AttrDefs<S,V> other : others) {
			addAll(other);

			// All of the native attributes from the old set should
			// have been implicitly transferred.
			boolean assertionsActive = false;
			assert assertionsActive = true;
			if (assertionsActive) {
				for (S attr : other.getNativeAttrs()) {
					assert nativeAttrs.contains(attr);
				}
			}
		}
		return this;
	}


	/**
	 * Specifies an iteration over the attribute definitions in the
	 * same order they were defined.
	 */
	public Iterator<ExtAttrDef<S,V>> iterator() {
		return Iterables.unmodifiableIterable(extAttrs.values()).iterator();
	}

	/**
	 * Gets the tag values for all native attributes used in this set.
	 * @return Set of native tag values
	 */
	public Set<S> getNativeAttrs() {
		return Collections.unmodifiableSet(nativeAttrs);
	}
}
