/*
 * dicom-xnat-util: org.nrg.dcm.Extractor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import org.dcm4che3.data.Attributes;

import java.util.SortedSet;

public interface Extractor {
    /**
     * Extract a value from the provided DICOM dataset.
     * @param attributes DICOM dataset
     * @return Attribute value, or null if none can be extracted.
     */
    String extract(Attributes attributes);

    /**
     * Get the DICOM tags on which this extraction operation depends.
     * @return DICOM tags
     */
    SortedSet<Integer> getTags();
}
