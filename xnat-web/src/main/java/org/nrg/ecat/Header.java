/*
 * ecat4xnat: org.nrg.ecat.Header
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ecat;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;

public interface Header {
	enum Type { MAIN, IMAGE }

	Type getType();
	
	Object set(Variable var, Object value);
	
	void read(ByteBuffer b, Collection<Variable> vars);
	
	void add(Map<Variable, ?> vars);
	
	Object get(Variable var);
	
	SortedMap<Variable,?> getValues();
	
	/**
	 * Generates a simple XML representation of the header contents
	 * @param out    The stream to write.
	 */
    void emitXML(PrintStream out);
}
