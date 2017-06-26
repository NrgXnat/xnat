/*
 * web: org.nrg.dcm.id.TextDicomIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.util.TagUtils;
import org.nrg.dicomtools.utilities.DicomUtils;
import org.nrg.framework.utilities.SortedSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedSet;

public final class TextDicomIdentifier implements DicomObjectFunction, DicomDerivedString {
    public TextDicomIdentifier(final int tag) {
        _tag = tag;
        _formatted = TagUtils.toString(tag);
        _name = DicomUtils.getDicomAttribute(tag);
        _log.trace("initialized {} {}", _formatted, _name);
    }

    /**
     * Gets the value of the DICOM header specified by the {@link #getTags()} property.
     */
    public String apply(final DicomObject dicomObject) {
        return dicomObject.getString(_tag);
    }

    /**
     * Gets all the tags associated with this identifier. For this class, there will always be just one tag.
     */
    public SortedSet<Integer> getTags() {
        return SortedSets.singleton(_tag);
    }

    /**
     * Displays the identifier's DICOM tag.
     */
    @Override
    public String toString() {
        return "DICOM tag " + _formatted + " " + _name;
    }

    private static final Logger _log = LoggerFactory.getLogger(TextDicomIdentifier.class);

    private final int    _tag;
    private final String _formatted;
    private final String _name;
}
