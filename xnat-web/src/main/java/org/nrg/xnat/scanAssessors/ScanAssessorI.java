/*
 * org.nrg.xnat.scanAssessors.ScanAssessorI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.scanAssessors;

public interface ScanAssessorI {
	public ScanAssessorScanI getScanById(String id);
	public String getHeader();
	public int getPrecedence();
}
