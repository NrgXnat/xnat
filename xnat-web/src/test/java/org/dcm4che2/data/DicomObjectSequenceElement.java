/*
 * dicom-xnat-mx: org.dcm4che2.data.DicomObjectSequenceElement
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.dcm4che2.data;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class DicomObjectSequenceElement extends SequenceDicomElement {
	private static final long serialVersionUID = 1L;
	
	public DicomObjectSequenceElement(final int tag,
			DicomObject parent, final Iterable<DicomObject> items) {
		super(tag, VR.SQ, parent.bigEndian(), buildObjectList(items), parent);
	}
	
	private static List<Object> buildObjectList(Iterable<?> items) {
		return ImmutableList.copyOf(items);
	}
}
