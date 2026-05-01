/*
 * dicom-xnat-util: org.nrg.dcm.MatchedPatternExtractor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.util.TagUtils;
import org.nrg.framework.utilities.SortedSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class MatchedPatternExtractorWithLengthLimit implements Extractor {
    private final Logger logger = LoggerFactory.getLogger(MatchedPatternExtractorWithLengthLimit.class);
    private final int tag, group, maxLength;
    private final Pattern pattern;

    public MatchedPatternExtractorWithLengthLimit(final int tag, final Pattern pattern, final int group, final int maxLength) {
        this.tag = tag;
        this.pattern = pattern;
        this.group = group;
        this.maxLength = maxLength;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.Extractor#extract(org.dcm4che2.data.DicomObject)
     */
    public String extract(final DicomObject o) {
        DicomElement el = o.get(tag);

        if (el == null || el.isEmpty()) {
            logger.trace("no match to {}: null or empty tag", this);
            return null;
        } else {
            String v;
            try {
                v = el.getString(o.getSpecificCharacterSet(), false);
            } catch (Exception e) {
                logger.error("Unable to getString for tag " + Integer.toHexString(tag), e);
                return null;
            }
            v = (maxLength < 0 || v.length() <= maxLength)? v: v.substring(0, maxLength);
            final Matcher m = pattern.matcher(v);
            if (m.matches()) {
                logger.trace("input {} matched rule {}", v, this);
                return m.group(group);
            } else {
                logger.trace("input {} did not match rule {}", v, this);
                return null;
            }
        }
    }

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
        sb.append(":").append(TagUtils.toString(tag)).append("~");
        sb.append(pattern).append("[").append(group).append("]");
        return sb.toString();
    }
}
