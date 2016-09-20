/*
 * core: org.nrg.xft.utils.ValidationUtils.ValidationResultsI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.utils.ValidationUtils;

public interface ValidationResultsI {

	/**
	 * If there were any erros then false, else true.
	 * @return
	 */
	public abstract boolean isValid();

}