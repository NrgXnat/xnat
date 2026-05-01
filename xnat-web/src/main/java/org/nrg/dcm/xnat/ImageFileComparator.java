/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.ImageFileComparator
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;

import org.nrg.attr.ExtAttrValue;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * 
 * Orders Files by DICOM Instance Number.
 * Files without an instance number always compare larger than Files with an instance number.
 * Files with equal instance number are ordered lexicographically by pathname.
 * @author Kevin A. Archie (karchie@wustl.edu)
 *
 */
public final class ImageFileComparator implements Comparator<File> {
	private static final String SORTING_ATTR = "instanceNumber";
	private final Multimap<File,? extends ExtAttrValue> values;

	public ImageFileComparator(final Multimap<File,? extends ExtAttrValue> values) {
		this.values = Multimaps.unmodifiableMultimap(values);
	}

	private String getAttributeValue(final File f, final String attr) {
		final Collection<? extends ExtAttrValue> fileValues = values.get(f);
		if (null == fileValues) {
			return null;
		}
		for (final ExtAttrValue eav : fileValues) {
			if (attr.equals(eav.getName())) {
				return eav.getText();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(final File f1, final File f2) {
		Integer i1, i2;
		try {
			i1 = Integer.valueOf(getAttributeValue(f1, SORTING_ATTR));
		} catch (NumberFormatException e) {
			i1 = null;
		}
		try {
			i2 = Integer.valueOf(getAttributeValue(f2, SORTING_ATTR));
		} catch (NumberFormatException e) {
			i2 = null;
		}

		if (null == i1) {
			if (null == i2) {
				return f1.compareTo(f2);	// compare pathnames lexicographically
			} else {
				return 1;
			}
		} else if (null == i2) {
			return -1;
		}
		if (i1.equals(i2)) {
			return f1.compareTo(f2);    // might be distinct files; compare pathnames lexicographically
		} else {
			return i1.compareTo(i2);
		}
	}
}
