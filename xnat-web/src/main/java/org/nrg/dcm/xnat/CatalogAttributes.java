/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.CatalogAttributes
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import org.dcm4che2.data.Tag;
import org.nrg.dcm.AttrDefs;
import org.nrg.dcm.MutableAttrDefs;

/**
 * Attributes for a cat:dcmCatalog object, cataloging the image files
 * that make up a single DICOM series.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
final class CatalogAttributes {
	private CatalogAttributes() {}    // no instantiation
	static public AttrDefs get() { return s; }

	static final private MutableAttrDefs s = new MutableAttrDefs();
	static {
		s.add("UID", Tag.SeriesInstanceUID);
		// dimensions also includes "z" and "volumes", but these are
		// handled explicitly by the SessionBuilder.
		s.add(new XnatAttrDef.AttributesOnly("dimensions",
				new String[]{"x", "y"}, new Integer[]{Tag.Columns, Tag.Rows}));
		s.add(new VoxelResAttribute("voxelRes"));
		s.add(new OrientationAttribute("orientation"));
	}
}
