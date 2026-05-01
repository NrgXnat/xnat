/*
 * ecat4xnat: org.nrg.ecat.xnat.ImageResourceAttributes
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ecat.xnat;

import static org.nrg.ecat.Variable.*;

import org.nrg.attr.AttrDefs;
import org.nrg.ecat.AttrDefSet;
import org.nrg.ecat.Variable;

final class ImageResourceAttributes {
	private ImageResourceAttributes() {}  // no instantiation

	static public AttrDefs<Variable> get() { return s; }

	static final private AttrDefSet s = new AttrDefSet();

	static {
		s.add("URI");         // determined explicitly in code
		s.add("format", MAGIC_NUMBER);
		s.add("content", "RAW");
		s.add(new XnatAttrDef.AttributesOnly("dimensions",
				new String[]{"x","y","z","volumes"},
				new Variable[]{X_DIMENSION,Y_DIMENSION,Z_DIMENSION,NUM_FRAMES}
		));
		s.add(new XnatAttrDef.AttributesOnly("voxelRes",
				new String[]{"x","y","z","units"},
				new Variable[]{X_PIXEL_SIZE,Y_PIXEL_SIZE,Z_PIXEL_SIZE,PIXEL_SIZE_UNITS}
		));
	}
}
