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
import org.nrg.framework.utilities.SortedSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedSet;

public final class TextDicomIdentifier implements DicomObjectFunction,DicomDerivedString {
    private final int tag;

    public TextDicomIdentifier(final int tag) {
        this.tag = tag;
        final Logger logger = LoggerFactory.getLogger(TextDicomIdentifier.class);
        if (logger.isTraceEnabled()) {
            logger.trace("initialized {}", TagUtils.toString(tag));
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.Extractor#extract(org.dcm4che2.data.DicomObject)
     */
    public String apply(final DicomObject o) { return o.getString(tag); }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.Extractor#getTags()
     */
    public SortedSet<Integer> getTags() {
        return SortedSets.singleton(tag);
    }


    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString());
        sb.append(":").append(TagUtils.toString(tag));
        return sb.toString();
    }

}
