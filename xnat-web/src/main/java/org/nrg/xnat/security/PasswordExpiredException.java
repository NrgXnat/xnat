/*
 * web: org.nrg.xnat.security.PasswordExpiredException
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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