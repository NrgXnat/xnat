/*
 * dicom-xnat-util: org.nrg.dcm.ChainExtractor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import java.util.Arrays;
import java.util.SortedSet;

import org.dcm4che2.data.DicomObject;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;

/**
 * Extractor that applies its constituent Extractors in order until
 * one produces a non-null-or-empty value.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class ChainExtractor implements Extractor {
    private final Iterable<Extractor> extractors;
    private final SortedSet<Integer> tags;

    public ChainExtractor(final Iterable<Extractor> extractors) {
        this.extractors = ImmutableList.copyOf(extractors);
        final SortedSet<Integer> ts = Sets.newTreeSet();
        for (final Extractor extractor : extractors) {
            ts.addAll(extractor.getTags());
        }
        this.tags = ImmutableSortedSet.copyOf(ts);

    }

    public ChainExtractor(final Extractor...extractors) {
        this(Arrays.asList(extractors));
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.Extractor#extract(org.dcm4che2.data.DicomObject)
     */
    public String extract(final DicomObject o) {
        for (final Extractor extractor : extractors) {
            final String v = extractor.extract(o);
            if (!Strings.isNullOrEmpty(v)) {
                return v;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.Extractor#getTags()
     */
    public SortedSet<Integer> getTags() {
        return tags;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString());
        sb.append(":");
        Joiner.on(",").appendTo(sb, extractors);
        return sb.toString();
    }
}
