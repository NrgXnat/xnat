/*
 * org.nrg.xdat.security.PasswordValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */
package org.nrg.xdat.security;

public interface PasswordValidator {
	
	public boolean isValid(String password, XDATUser user);
	public String getMessage();
}
