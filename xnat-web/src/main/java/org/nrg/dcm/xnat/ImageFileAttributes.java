/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.ImageFileAttributes
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
 * Attributes for a dcmEntry (DICOM file entry for catalog)
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class ImageFileAttributes {
	private ImageFileAttributes() {}    // no instantiation
	static public AttrDefs get() { return s; }

	public static final String SOPClassUID = "SOP_Class_UID";

	static final private MutableAttrDefs s = new MutableAttrDefs();
	static {
		s.add(SOPClassUID, Tag.SOPClassUID);    // required by SessionBuilder
		s.add("URI");
		s.add("UID", Tag.SOPInstanceUID);
		s.add(new XnatAttrDef.Int("instanceNumber", Tag.InstanceNumber, 1));
		s.add(new XnatAttrDef.Int("numFrames", Tag.NumberOfFrames, 1));	// used for scan dimensions
	}
}
