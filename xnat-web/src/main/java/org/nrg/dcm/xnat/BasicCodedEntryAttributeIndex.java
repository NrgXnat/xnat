/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.BasicCodedEntryAttributeIndex
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.nrg.dcm.AbstractDicomAttributeIndex;
import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.dcm.MergedDicomElement;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

/**
 * Describes an attribute derived from a Code Sequence as described in PS 3.3 Section 8
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class BasicCodedEntryAttributeIndex
extends AbstractDicomAttributeIndex implements DicomAttributeIndex {
	private static final int DEFAULT_VALUE_TAG = Tag.CodeMeaning;

	private final Integer[] sequencePath;
	private final String codingSchemeDesignator, codingSchemeVersion;
	private final String columnName;
	private final int valueTag;

	private String attributeName = null;

	/**
	 * 
	 */
	public BasicCodedEntryAttributeIndex(final Integer[] sequencePath,
			final String codingSchemeDesignator, final String codingSchemeVersion,
			final String columnName, final int valueTag) {
		this.sequencePath = new Integer[sequencePath.length];
		System.arraycopy(sequencePath, 0, this.sequencePath, 0, sequencePath.length);
		this.codingSchemeDesignator = codingSchemeDesignator;
		this.codingSchemeVersion = codingSchemeVersion;
		this.columnName = columnName;
		this.valueTag = valueTag;
	}

	public BasicCodedEntryAttributeIndex(final Integer[] sequencePath,
			final String codingSchemeDesignator, final String codingSchemeVersion,
			final String columnName) {
		this(sequencePath, codingSchemeDesignator, codingSchemeVersion, columnName, DEFAULT_VALUE_TAG);
	}

	public BasicCodedEntryAttributeIndex(final Integer[] sequencePath,
			final String codingSchemeDesignator, final String codingSchemeVersion) {
		this(sequencePath, codingSchemeDesignator, codingSchemeVersion,
				buildColumnName(sequencePath, codingSchemeDesignator, codingSchemeVersion),
				DEFAULT_VALUE_TAG);
	}

	private static String buildColumnName(final Integer[] sequencePath,
			final String designator, final String version) {
		final StringBuilder sb = new StringBuilder("cosq");
		for (final int elem : sequencePath) {
			sb.append(String.format("%08x", elem));
		}
		sb.append("_").append(makeColumnName(designator));
		sb.append("_").append(makeColumnName(version));
		return sb.toString();
	}

	private static String makeColumnName(final String s) {
		return s.replaceAll("[^\\w]", "_");
	}

	/* (non-Javadoc)
	 * @see org.nrg.dcm.DicomAttributeIndex#getAttributeName(org.dcm4che2.data.DicomObject)
	 */
	public String getAttributeName(final DicomObject unused) {
		if (null == attributeName) {
			final StringBuilder sb = new StringBuilder();
			sb.append(unused.nameOf(sequencePath[sequencePath.length - 1]));
			sb.append("[").append(codingSchemeDesignator);
			sb.append(":v").append(codingSchemeVersion).append("]");
			attributeName = sb.toString();
		}
		return attributeName;
	}

	/* (non-Javadoc)
	 * @see org.nrg.dcm.DicomAttributeIndex#getColumnName()
	 */
	public String getColumnName() {
		return columnName;
	}

	/* (non-Javadoc)
	 * @see org.nrg.dcm.DicomAttributeIndex#getElement(org.dcm4che2.data.DicomObject)
	 */
	public DicomElement getElement(final DicomObject o) {
	    final Set<DicomElement> sqs = AbstractDicomAttributeIndex.getElements(o, sequencePath, 0, new LinkedHashSet<DicomElement>());
	    if (sqs.isEmpty()) {
	        return null;
	    }
	    final Set<DicomElement> ves = Sets.newLinkedHashSet();
	    for (final DicomElement sq : sqs) {
	        for (int i = 0, n = sq.countItems(); i < n; i++) {
	            final DicomObject elem = sq.getDicomObject(i);
	            if (codingSchemeDesignator.equals(elem.getString(Tag.CodingSchemeDesignator)) &&
	                    codingSchemeVersion.equals(elem.getString(Tag.CodingSchemeVersion))) {
	                final DicomElement ve = elem.get(valueTag);
	                if (null != ve) {
	                    ves.add(ve);
	                }
	                break;
	            }
	        }
		}
	    final Iterator<DicomElement> vesi = ves.iterator();
	    if (!vesi.hasNext()) {
	        return null;
	    }
	    final DicomElement ve0 = vesi.next();
	    if (vesi.hasNext()) {
	        return new MergedDicomElement(o, ve0, Iterators.toArray(vesi, DicomElement.class));
	    } else {
	        return ve0;
	    }
	}

	/* (non-Javadoc)
	 * @see org.nrg.dcm.DicomAttributeIndex#getPath(org.dcm4che2.data.DicomObject)
	 */
	public Integer[] getPath(final DicomObject unused) {
		return Arrays.copyOf(sequencePath, sequencePath.length);
	}
}
