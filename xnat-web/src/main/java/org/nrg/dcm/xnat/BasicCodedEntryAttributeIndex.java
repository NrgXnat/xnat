/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.BasicCodedEntryAttributeIndex
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import lombok.Getter;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.util.TagUtils;
import org.nrg.dcm.AbstractDicomAttributeIndex;
import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.dicomtools.utilities.DicomUtils;

import java.util.Arrays;


/**
 * Describes an attribute derived from a Code Sequence as described in PS 3.3 Section 8
 *
 */
public final class BasicCodedEntryAttributeIndex extends AbstractDicomAttributeIndex implements DicomAttributeIndex {
	private static final int DEFAULT_VALUE_TAG = Tag.CodeMeaning;

	private final Integer[] sequencePath;
    private final Integer[] codingSchemeDesignatorPath;
	private final String codingSchemeDesignator;
	private final String codingSchemeVersion;
    @Getter
	private final String columnName;
	private final int valueTag;

	private String attributeName = null;

	public BasicCodedEntryAttributeIndex(final Integer[] sequencePath,
			final String codingSchemeDesignator, final String codingSchemeVersion,
			final String columnName, final int valueTag) {
		this.sequencePath = new Integer[sequencePath.length];
		System.arraycopy(sequencePath, 0, this.sequencePath, 0, sequencePath.length);
		this.codingSchemeDesignator = codingSchemeDesignator;
		this.codingSchemeVersion = codingSchemeVersion;
		this.columnName = columnName;
        this.codingSchemeDesignatorPath = new Integer[sequencePath.length + 2];
        System.arraycopy(sequencePath, 0, codingSchemeDesignatorPath, 0, sequencePath.length);
        codingSchemeDesignatorPath[sequencePath.length] = null;
        codingSchemeDesignatorPath[sequencePath.length+1] = Tag.CodingSchemeDesignator;
		this.valueTag = valueTag;
	}

	public BasicCodedEntryAttributeIndex(
			final Integer[] sequencePath,
			final String codingSchemeDesignator,
			final String codingSchemeVersion,
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
			sb.append("%08x".formatted(elem));
		}
		sb.append("_").append(makeColumnName(designator));
		sb.append("_").append(makeColumnName(version));
		return sb.toString();
	}

	private static String makeColumnName(final String s) {
		return s.replaceAll("[^\\w]", "_");
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAttributeName(final Attributes attributes) {
		if (null == attributeName) {
            attributeName = DicomUtils.getAttributeName(attributes, sequencePath[sequencePath.length - 1]) +
                    "[" + codingSchemeDesignator + ":v" + codingSchemeVersion + "]";
		}
		return attributeName;
	}

	/**
     * {@inheritDoc}
	 */
	@Override
	public int getMaxTag() {
		// codingSchemeDesignatorPath is sequencePath extended, so both enter at the same tag, and valueTag is
		// read out of a nested context by getStrings rather than from the top level. The sequence is the bound.
		return entryTagOf(sequencePath);
	}

	/**
     * {@inheritDoc}
	 */
	@Override
	public Integer[] getPath(final Attributes unused) {
		return Arrays.copyOf(sequencePath, sequencePath.length);
	}

    @Override
    public String[] getStrings(final Attributes attributes) {
        return getNestedContexts(attributes, codingSchemeDesignatorPath).stream()
                .filter(context ->
                        codingSchemeDesignator.equals(context.getString(Tag.CodingSchemeDesignator)))
                .filter(context ->
                        codingSchemeVersion.equals(context.getString(Tag.CodingSchemeVersion)))
                .map(context -> DicomUtils.combineValues(context.getStrings(valueTag)))
                .toArray(String[]::new);
    }
}
