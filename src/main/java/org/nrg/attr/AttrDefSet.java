/**
 * Copyright (c) 2006-2010 Washington University
 */
package org.nrg.attr;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Collection;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;


/**
 * Describes a group of external attributes and their conversions from native fields
 * @author Kevin A. Archie <karchie@wustl.edu>
 *
 */
public class AttrDefSet<S,V> implements ReadableAttrDefSet<S,V> {
	final private Map<String,ExtAttrDef<S,V>> extAttrs = new LinkedHashMap<String,ExtAttrDef<S,V>>();
	final private Set<S> nativeAttrs;

	public AttrDefSet(final Comparator<S> comparator) {
		nativeAttrs = new TreeSet<S>(comparator);
	}

	public AttrDefSet() {
		this(null);
	}

	@SuppressWarnings("unchecked")
	public AttrDefSet(final Comparator<S> comparator,
			final ReadableAttrDefSet<S,V> base, final ExtAttrDef<S,V>...adds) {
		this(comparator);
		this.add(base);
		this.addAll(Arrays.asList(adds));
	}

	public AttrDefSet(final ReadableAttrDefSet<S,V> base, final ExtAttrDef<S,V>...adds) {
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
		final String name = a.getName();
		if (extAttrs.containsKey(name)) {
			throw new IllegalArgumentException("Redefined external attribute " + name);
		}
		synchronized (this) {
			extAttrs.put(name, a);
			nativeAttrs.addAll(a.getAttrs());
		}
		return a;
	}

	/**
	 * Adds multiple external attributes to this set.
	 * @param Collection of external attribute specifications
	 */
	public AttrDefSet<S,V> addAll(Collection<? extends ExtAttrDef<S,V>> as) {
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
	 * There should be no overlapping attribute definitions.
	 * @param other AttrDefSet from which attributes are to be copied.
	 */
	public AttrDefSet<S,V> add(ReadableAttrDefSet<S,V>...others) {
		for (ReadableAttrDefSet<S,V> other : others) {
			for (final ExtAttrDef<S,V> ea : other) {
				assert !extAttrs.containsKey(ea.getName());
				add(ea);
			}

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
	 * Removes the named attribute from this set.
	 * @param attr name of the external attribute
	 * @return The number of attributes removed (1 if present, 0 otherwise)
	 */
	public int remove(String name) {
		return (null == extAttrs.remove(name)) ? 0 : 1;
	}


	/**
	 * Removes any external attributes using the indicated native attribute from this set.
	 * @param attr native attribute to be removed
	 * @return The number of external attributes removed
	 */
	public int remove(final S attr) {
		int count = 0;

		synchronized (this) {
			for (Iterator<Map.Entry<String,ExtAttrDef<S,V>>> i = extAttrs.entrySet().iterator(); i.hasNext(); ) {
				final Map.Entry<String,ExtAttrDef<S,V>> e = i.next();
				if (e.getValue().getAttrs().contains(attr)) {
					i.remove();
					count++;
				}
			}

			// Rebuild the native attributes set
			nativeAttrs.clear();
			for (ExtAttrDef<S,V> ea : this) {
				nativeAttrs.addAll(ea.getAttrs());
			}
		}

		return count;
	}

	/**
	 * Specifies an iteration over the attribute definitions in the
	 * same order they were defined.
	 */
	public Iterator<ExtAttrDef<S,V>> iterator() {
		return Collections.unmodifiableCollection(extAttrs.values()).iterator();
	}


	/**
	 * @param name name of an external attribute defined in this set
	 * @return attribute definition object
	 */
	public ExtAttrDef<S,V> getExtAttrDef(final String name) {
		return extAttrs.get(name);
	}


	/**
	 * Gets the tag values for all native attributes used in this set.
	 * @return Set of native tag values
	 */
	public Set<S> getNativeAttrs() {
		return Collections.unmodifiableSet(nativeAttrs);
	}
}
