/*
 * web: org.nrg.xnat.scanAssessors.ScanAssessorI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.scanAssessors;

public interface ScanAssessorI {
	public ScanAssessorScanI getScanById(String id);
	public String getHeader();
	public int getPrecedence();
}
