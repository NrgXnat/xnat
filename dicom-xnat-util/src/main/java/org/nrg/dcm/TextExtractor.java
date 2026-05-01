/*
 * dicom-xnat-util: org.nrg.dcm.TextExtractor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import java.util.SortedSet;

import org.dcm4che2.data.DicomObject;
import org.nrg.framework.utilities.SortedSets;

/**
 * 
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class TextExtractor implements Extractor {
    private final int tag;
    
    public TextExtractor(final int tag) {
        this.tag = tag;
    }
    
    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.Extractor#extract(org.dcm4che2.data.DicomObject)
     */
    public String extract(final DicomObject o) { return o.getString(tag); }
    
    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.Extractor#getTags()
     */
    public SortedSet<Integer> getTags() {
        return SortedSets.singleton(tag);
    }
}
