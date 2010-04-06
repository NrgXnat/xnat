//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Nov 24, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.nrg.xft.exception;

import org.nrg.xft.utils.ValidationUtils.ValidationResults;

/**
 * @author Tim
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
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

