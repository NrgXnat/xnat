/*
 * org.nrg.dcm.id.DicomObjectFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.dcm.id;

import java.util.SortedSet;


public interface DicomObjectFunction {
    SortedSet<Integer> getTags();
}
