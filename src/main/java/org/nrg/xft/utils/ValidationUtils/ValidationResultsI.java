/*
 * core: org.nrg.xft.utils.ValidationUtils.ValidationResultsI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.utils.ValidationUtils;

public interface ValidationResultsI {

	/**
	 * If there were any errors then false, else true.
	 * @return Returns whether there were any errors
	 */
	public abstract boolean isValid();

}
