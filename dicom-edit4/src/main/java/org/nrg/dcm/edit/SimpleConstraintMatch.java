/*
 * DicomEdit: org.nrg.dcm.edit.SimpleConstraintMatch
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import java.util.SortedSet;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.VR;
import org.dcm4che2.util.StringUtils;
import org.dcm4che2.util.TagUtils;

import com.google.common.collect.ImmutableSortedSet;
import org.nrg.dicom.mizer.objects.DicomObjectI;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
class SimpleConstraintMatch implements ConstraintMatch {
    private final int tag;
    private final String pattern;

    SimpleConstraintMatch(final int tag, final String pattern) {
        this.tag = tag;
        this.pattern = pattern;
    }

    SimpleConstraintMatch(final Integer tag, final String pattern) {
        this(tag.intValue(), pattern);
    }

    public SortedSet<Long> getTags() {
        return ImmutableSortedSet.of(0xffffffffL & tag);
    }

    final String getPattern() { return pattern; }

    boolean matches(final String value) {
        return value.equals(pattern);
    }

    public boolean matches(final DicomObjectI dicomObject) {
        if (dicomObject.contains(tag)) {
            final DicomObject dcm4che2Object = dicomObject.getDcm4che2Object();
            final VR vr = dcm4che2Object.vrOf(tag);
            final String value;
            if (VR.SQ == vr) { 
                throw new RuntimeException("can't use SQ type attribute for constraint");
            } else if (VR.UN == vr) {
                value = dicomObject.getString(tag);
            } else {
                value = StringUtils.join(dcm4che2Object.getStrings(tag), '\\');
            }
            return matches(value);
        } else
            return false;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Constraint: " + TagUtils.toString(tag) + " matches " + pattern;
    }
}
