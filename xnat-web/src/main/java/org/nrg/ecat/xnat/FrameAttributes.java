/*
 * ecat4xnat: org.nrg.ecat.xnat.FrameAttributes
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ecat.xnat;

import org.nrg.attr.AttrDefs;
import org.nrg.ecat.AttrDefSet;
import org.nrg.ecat.Variable;

final class FrameAttributes {
	private FrameAttributes() {}	// no instantiation

	static public AttrDefs<Variable> get() { return s; }

	static final private AttrDefSet s = new AttrDefSet();

	static {
		s.add("number");	// incrementing number, filled in by session builder
		s.add("starttime", Variable.FRAME_START_TIME);
		s.add("length", Variable.FRAME_DURATION);
		s.add("units", "sec");	// session builder converts from msec
	}
}
