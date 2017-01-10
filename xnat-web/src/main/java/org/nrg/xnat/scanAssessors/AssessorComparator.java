/*
 * web: org.nrg.xnat.scanAssessors.AssessorComparator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.scanAssessors;

import java.util.Comparator;


public class AssessorComparator implements Comparator<ScanAssessorI>{

	public int compare(ScanAssessorI o1, ScanAssessorI o2) {
		return ((Integer)o1.getPrecedence()).compareTo((Integer)o2.getPrecedence());
	}
	
}
