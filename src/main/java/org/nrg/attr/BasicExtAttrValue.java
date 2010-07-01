/**
 * Copyright (c) 2006-2010 Washington University
 */
package org.nrg.attr;

import java.lang.IllegalArgumentException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;


/**
 * Represents the value of an external attribute element.
 * A ExtAttrValue may have a text value (corresponding to XML element text),
 * attribute values (element attributes), or both.
 * @author Kevin A. Archie <karchie@wustl.edu>
 *
 */
public final class BasicExtAttrValue implements ExtAttrValue {
	private final String name;
	private final String textValue;
	private final Map<String,String> attrValues = Maps.newLinkedHashMap();

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof BasicExtAttrValue)) return false;
		final BasicExtAttrValue ov = (BasicExtAttrValue) o;
		if (textValue != ov.textValue) {
			if (textValue == null || ov.textValue == null) return false;
			if (!textValue.equals(ov.textValue)) return false;
		}
		assert textValue == ov.textValue || textValue.equals(ov.textValue);
		if (attrValues == ov.attrValues) return true;
		if (attrValues == null || ov.attrValues == null) return false;
		return (attrValues.equals(ov.attrValues));
	}

	@Override
	public int hashCode() {
		int code = 17;      // generate a good hashCode a la Bloch
		assert name != null;
		code = 37*code + name.hashCode();
		if (textValue != null) {
			code = 37*code + textValue.hashCode();
		}
		if (attrValues != null) {
			code = 37*code + attrValues.hashCode();
		}
		return code;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("<");
		sb.append(name);
		for (final String attrName : attrValues.keySet()) {
			sb.append(" ");
			sb.append(attrName);
			sb.append("=\"");
			sb.append(attrValues.get(attrName));
			sb.append("\"");
		}
		if (null == textValue) {
			sb.append("/>");
		} else {
			sb.append(">");
			sb.append(textValue);
			sb.append("</");
			sb.append(name);
			sb.append(">");
		}
		return sb.toString();
	}


	public BasicExtAttrValue(final String name, final String value, final Map<String,String> attrValues) {
		if (null == name) {
			throw new IllegalArgumentException("ExtAttrValue name must be non-null");
		}
		this.name = name;
		textValue = value;
		if (null != attrValues) {
			this.attrValues.putAll(attrValues);
		}
	}

	public BasicExtAttrValue(final String name, final String value) {
		this(name, value, null);
	}

	public BasicExtAttrValue(final Collection<ExtAttrValue> toMerge) {
		this(extractMergedName(toMerge.iterator()),
				mergeText(",", toMerge.iterator()),
				mergeAttrs(",", toMerge.iterator()));
	}

	@SuppressWarnings("unchecked")
	public BasicExtAttrValue(final ExtAttrValue base, final Map<String,String> attrValues) {
		this(base.getName(), base.getText(), Utils.merge(base.getAttrs(), attrValues));
	}

	private final static String extractMergedName(final Iterator<ExtAttrValue> vi) {
		final Set<String> names = Sets.newLinkedHashSet();
		while (vi.hasNext()) {
			names.add(vi.next().getName());
		}
		if (1 == names.size()) {
			return names.iterator().next();
		} else {
			throw new IllegalArgumentException("values have no or multiple names: " + names);
		}
	}

	private final static boolean isEmptyText(final String s) {
		return null == s || "".equals(s);
	}

	private final static String mergeText(final String separator, final Iterator<ExtAttrValue> vi) {
		final Set<String> textvs = Sets.newLinkedHashSet();
		while (vi.hasNext()) {
			final String text = vi.next().getText();
			if (!isEmptyText(text)) {
				textvs.add(text);
			}
		}
		final StringBuilder sb = new StringBuilder();
		boolean isEmpty = true;
		for (final String text : textvs) {
			if (!isEmpty) {
				sb.append(separator);
			}
			sb.append(text);
			isEmpty = false;
		}
		return sb.toString();
	}

	private final static Map<String,String> mergeAttrs(final String separator, final Iterator<ExtAttrValue> vi) {
		final SetMultimap<String,String> vals = LinkedHashMultimap.create();
		while (vi.hasNext()) {
			final ExtAttrValue v = vi.next();
			for (final Map.Entry<String,String> me : v.getAttrs().entrySet()) {
				vals.put(me.getKey(), me.getValue());
			}
		}

		final Joiner joiner = Joiner.on(separator);
		final Map<String,String> merged = Maps.newLinkedHashMap();
		for (final Map.Entry<String,Collection<String>> vme : vals.asMap().entrySet()) {
			merged.put(vme.getKey(), joiner.join(vme.getValue()));
		}
		return merged;
	}



	/**
	 * Adds an attribute to the value element.
	 * @param name attribute name
	 * @param value
	 */
	protected void addAttr(final String name, final String value) {
		if (attrValues.containsKey(name)) {
			throw new IllegalArgumentException("Redefined value attribute " + name);
		}
		attrValues.put(name,value);
	}

	/**
	 * Returns the name of this attribute.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the element text for this value.
	 */
	public String getText() {
		return textValue;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nrg.attr.ExtAttrValue#getAttrs()
	 */
	public Map<String,String> getAttrs() {
		return Collections.unmodifiableMap(attrValues);
	}

	/**
	 * Returns the names of all attributes in this value.
	 */
	public Set<String> getAttrNames() {
		return Collections.unmodifiableSet(attrValues.keySet());
	}

	/**
	 * Returns the value of a specific attribute, given the attribute name.
	 * @param name name of the attribute
	 * @return attribute value
	 */
	public String getAttrValue(final String name) {
		return attrValues.get(name);
	}
}