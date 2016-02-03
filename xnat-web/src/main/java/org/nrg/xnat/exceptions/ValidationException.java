/*
 * org.nrg.xnat.exceptions.ValidationException
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
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
