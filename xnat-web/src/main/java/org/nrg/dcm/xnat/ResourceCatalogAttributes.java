/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.ResourceCatalogAttributes
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import org.nrg.dcm.AttrDefs;
import org.nrg.dcm.MutableAttrDefs;

/**
 * Attributes for a resourceCatalog element,
 * used in mrScanData/file to point to a DICOM file catalog.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
final class ResourceCatalogAttributes {
	private ResourceCatalogAttributes() {}    // no instantiation
	static public AttrDefs get() { return s; }

	static final private MutableAttrDefs s = new MutableAttrDefs();

	static {
		s.add("URI");	// filled in by 
		s.add("format", "DICOM");
		s.add("content", "RAW");
	}
}
