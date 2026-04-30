/*
 * DicomEdit: org.nrg.dcm.edit.SimpleConstraintMatch
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import com.google.common.collect.ImmutableSortedSet;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.util.SortedSet;

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
            final VR vr = VR.valueOf(dicomObject.getVR(tag));
            final String value;
            if (VR.SQ == vr) { 
                throw new RuntimeException("can't use SQ type attribute for constraint");
            } else if (VR.UN == vr) {
                value = dicomObject.getString(tag);
            } else {
                value = StringUtils.concat(dicomObject.getStrings(tag), '\\');
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
