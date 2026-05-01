/*
 * dicom-xnat-util: org.nrg.dcm.Extractor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import java.util.SortedSet;

import org.dcm4che2.data.DicomObject;

/**
 * 
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public interface Extractor {
    String extract(DicomObject o);
    SortedSet<Integer> getTags();
}
