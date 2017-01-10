/*
 * web: org.nrg.xnat.exceptions.ValidationException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.exceptions;


/**
 * @author Timothy R. Olsen <olsent@wustl.edu>
 *
 */
public class ValidationException extends Exception {
	public ValidationException(String s){
		super(s);
	}
}
