/*
 * web: org.nrg.dcm.id.DicomObjectFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import java.util.SortedSet;


public interface DicomObjectFunction {
    SortedSet<Integer> getTags();
}
