/*
 * org.nrg.xnat.security.PasswordExpiredException
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.security;


public class PasswordExpiredException extends RuntimeException{
	
	
	public PasswordExpiredException(String msg){
		super(msg);
	}
	
	public PasswordExpiredException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
}