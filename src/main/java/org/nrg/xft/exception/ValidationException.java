/*
 * org.nrg.xft.exception.ValidationException
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.exception;

import org.nrg.xft.utils.ValidationUtils.ValidationResults;

@SuppressWarnings("serial")
public class ValidationException extends Exception{
	public ValidationResults VALIDATION_RESULTS = null;
	public ValidationException(ValidationResults vr)
	{
		super("XML Validation Failed.");
		this.VALIDATION_RESULTS = vr;
	}
	
	public String toString()
	{
	    return this.VALIDATION_RESULTS.toFullString();
	}
}

