/*
 * dicom-xnat-util: org.nrg.dcm.MatchedPatternExtractor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.util.TagUtils;
import org.nrg.framework.utilities.SortedSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

    /**
     * {@inheritDoc}
     */
    @Override
    public String extract(final Attributes attributes) {
        String tagValue;
        try {
            tagValue = attributes.getString(tag);
        } catch (Exception e) {
            logger.error("Unable to getString for tag " + Integer.toHexString(tag), e);
            return null;
        }
        if (tagValue == null || tagValue.isEmpty()) {
            logger.trace("no match to {}: null or empty tag", this);
            return null;
        }
        tagValue = (maxLength < 0 || tagValue.length() <= maxLength) ? tagValue : tagValue.substring(0, maxLength);
        final Matcher m = pattern.matcher(tagValue);
        if (m.matches()) {
            logger.trace("input {} matched rule {}", tagValue, this);
            return m.group(group);
        } else {
            logger.trace("input {} did not match rule {}", tagValue, this);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Integer> getTags() {
        return SortedSets.singleton(tag);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString());
        sb.append(":").append(TagUtils.toString(tag)).append("~");
        sb.append(pattern).append("[").append(group).append("]");
        return sb.toString();
    }
}
