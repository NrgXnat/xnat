/*
 * org.nrg.xdat.security.RegExpValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */
package org.nrg.xdat.security;

import org.nrg.xft.security.UserI;

import java.util.regex.Pattern;

public class RegExpValidator implements PasswordValidator{

	String regexp="";
	String message="Password is not sufficiently complex.";
	
	public RegExpValidator(){
		
	}
	
	@Override
	public boolean isValid(String password, UserI user) {
		boolean valid = false;
		if((regexp.equals(""))){
			valid = true;
		}
		else{
			valid = Pattern.matches(regexp, password);
		}
		return valid;
	}

	public String getRegexp() {
		return regexp;
	}

	public void setRegexp(String regexp) {
		if(regexp!=null && !(regexp.equals(""))){
			this.regexp = regexp;
		}
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		if(message!=null && !(message.equals(""))){
			this.message = message;
		}
	}

}
