/*
 * dicom-xnat-util: org.nrg.dcm.ChainExtractor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import org.dcm4che3.data.Attributes;

import java.util.Arrays;
import java.util.SortedSet;

/**
 * Extractor that applies its constituent Extractors in order until
 * one produces a non-null-or-empty value.
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String extract(final Attributes attributes) {
        for (final Extractor extractor : extractors) {
            final String v = extractor.extract(attributes);
            if (!Strings.isNullOrEmpty(v)) {
                return v;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Integer> getTags() {
        return tags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString());
        sb.append(":");
        Joiner.on(",").appendTo(sb, extractors);
        return sb.toString();
    }
}
