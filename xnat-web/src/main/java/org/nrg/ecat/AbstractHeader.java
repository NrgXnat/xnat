/*
 * ecat4xnat: org.nrg.ecat.AbstractHeader
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ecat;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractHeader implements Header {
	private final Logger logger = LoggerFactory.getLogger(AbstractHeader.class);
	private final Header.Type type;
	private final SortedMap<Variable,Object> values;

	private AbstractHeader(final Header.Type type, SortedMap<Variable,Object> v) {
		this.type = type;
		this.values = v;
	}

	AbstractHeader(final Header template) {
		this(template.getType(), new TreeMap<>(template.getValues()));
	}

	AbstractHeader(final Header.Type type) {
		this(type, new TreeMap<Variable,Object>());
	}

	/*
	 * (non-Javadoc)
	 * @see org.nrg.ecat.ECATHeader#getType()
	 */
	public final Header.Type getType() { return type; }

	/*
	 * (non-Javadoc)
	 * @see org.nrg.ecat.ECATHeader#read(java.nio.ByteBuffer, java.util.Collection)
	 */
	public final void read(final ByteBuffer bbuf, final Collection<Variable> vars) {
		for (final Variable v : new TreeSet<>(vars)) {
			if (type.equals(v.header)) {
				set(v, v.getValue(bbuf));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nrg.ecat.ECATHeader#set(org.nrg.ecat.ECATVar, java.lang.Object)
	 */
	public final Object set(final Variable var, Object value) {
		if (type.equals(var.header)) {
			return values.put(var, value);
		} else {
			logger.warn("Tried to set mismatched ECAT variable " + var + " in header " + this);
			return null;
		}
	}

	/**
	 * Add only those assignments appropriate to this header
	 * @param assignments    The assignments to add.
	 */
	public void add(final Map<Variable,?> assignments) {
		for (final Map.Entry<Variable,?> me : assignments.entrySet()) {
			if (type.equals(me.getKey().header)) {
				values.put(me.getKey(), me.getValue());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nrg.ecat.ECATHeader#get(org.nrg.ecat.ECATVar)
	 */
	public Object get(final Variable var) {
		return values.get(var);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nrg.ecat.ECATHeader#getValues()
	 */
	public SortedMap<Variable,?> getValues() {
		return Collections.unmodifiableSortedMap(values);
	}

	private static final String attrFormat = "<var name=\"%1$s\" offset=\"%2$d\" type=\"%3$s\"%4$s>%5$s</var>";
	private static final String arrayFormat = "<v>%2$s</v>";
	private static final Map<String,String> types = new HashMap<>();
	static {
		types.put("java.lang.String","xs:string");
		types.put("java.lang.Integer","xs:integer");
		types.put("int","xs:integer");
		types.put("java.lang.Short","xs:short");
		types.put("short","xs:short");
		types.put("java.lang.Float", "xs:float");
		types.put("float","xs:float");
	}

	/**
	 * Emit any text needed to start the XML representation of this header.
	 * @param out    The stream to write to.
	 */
	protected void startXML(final PrintStream out) { }

	/**
	 * Emit any text needed to end the XML representation of this header.
	 * @param out    The stream to write to.
	 */
	protected void endXML(final PrintStream out) { }

	/*
	 * (non-Javadoc)
	 * @see org.nrg.ecat.ECATHeader#emitXML(java.io.PrintWriter)
	 */
	public final void emitXML(final PrintStream out) {
		startXML(out);
		for (final Map.Entry<Variable,?> e : values.entrySet()) {
			final Variable v = e.getKey();
			final Object val = e.getValue();
			final Class<?> c = val.getClass();
			if (c.isArray()) {
				final StringBuilder sb = new StringBuilder();
				final int length = Array.getLength(val);
				for (int i = 0; i < length; i++) {
					sb.append(String.format(arrayFormat, i, Array.get(val, i)));
				}
				out.println(String.format(attrFormat, v.name, v.offset,
						types.get(c.getComponentType().getName()),
						" count=\"" + length + "\"", sb.toString()));
			} else {
				out.println(String.format(attrFormat, v.name, v.offset,
						types.get(c.getName()), "", val));
			}
		}
		endXML(out);
	}
}
