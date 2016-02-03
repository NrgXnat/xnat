/*
 * org.nrg.dcm.id.PatternDicomIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.dcm.id;

import com.google.common.base.Strings;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.util.TagUtils;
import org.nrg.dcm.MatchedPatternExtractor;
import org.nrg.framework.utilities.SortedSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PatternDicomIdentifier implements DicomDerivedString {
    private final Logger logger = LoggerFactory.getLogger(MatchedPatternExtractor.class);
    private final int tag, group;
    private final Pattern pattern;

    public PatternDicomIdentifier(final int tag, final Pattern pattern, final int group) {
        this.tag = tag;
        this.pattern = pattern;
        this.group = group;
        if (logger.isTraceEnabled()) {
            logger.trace("initialized {}", getDescription());
        }
    }
    
    public PatternDicomIdentifier(final int tag, final Pattern pattern) {
        this(tag, pattern, 0);
    }
    
    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.Extractor#extract(org.dcm4che2.data.DicomObject)
     */
    public String apply(final DicomObject o) {
        final String v = o.getString(tag);
        if (Strings.isNullOrEmpty(v)) {
            logger.trace("no match to {}: null or empty tag", this);
            return null;
        } else {
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

    public SortedSet<Integer> getTags() {
        return SortedSets.singleton(tag);
    }

    private StringBuilder appendDescription(final StringBuilder sb) {
        sb.append(TagUtils.toString(tag)).append("~");
        sb.append(pattern).append("[").append(group).append("]");
        return sb;
    }
    
    private String getDescription() {
        return appendDescription(new StringBuilder()).toString();
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString());
        sb.append(":");
        appendDescription(sb);
        return sb.toString();
    }
}
