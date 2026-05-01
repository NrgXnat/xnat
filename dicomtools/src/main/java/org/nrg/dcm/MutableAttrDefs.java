/*
 * DicomDB: org.nrg.dcm.MutableAttrDefs
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;


/**
 * Describes a group of external attributes and their conversions from DICOM fields
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class MutableAttrDefs
extends org.nrg.attr.MutableAttrDefs<DicomAttributeIndex> implements AttrDefs {
	public MutableAttrDefs() {
		super(new DicomAttributeIndex.Comparator());
	}
	
	@SuppressWarnings("unchecked")
    public MutableAttrDefs(final AttrDefs attrs) {
		super(new DicomAttributeIndex.Comparator(), attrs);
	}
	
	public void add(final String name, final int tag) {
		super.add(name, new FixedDicomAttributeIndex(tag));
	}
}
