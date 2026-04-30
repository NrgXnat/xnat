/*
 * dicom-xnat-util: org.nrg.dcm.TextExtractor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import org.dcm4che3.data.Attributes;
import org.nrg.framework.utilities.SortedSets;

import java.util.SortedSet;


public class TextExtractor implements Extractor {
    private final int tag;
    
    public TextExtractor(final int tag) {
        this.tag = tag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String extract(final Attributes attributes) {
        return attributes.getString(tag);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Integer> getTags() {
        return SortedSets.singleton(tag);
    }
}
