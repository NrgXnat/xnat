/*
 * org.nrg.xdat.security.PasswordValidatorChain
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */
package org.nrg.xdat.security;

import java.util.List;

import org.nrg.xft.security.UserI;

public class PasswordValidatorChain implements PasswordValidator {
	List<PasswordValidator> validators;
	String message;
	
	//if there are no validators, just return true;
	@Override
	public boolean isValid(String password, UserI user){
		boolean ret = true;
		StringBuffer sb = new StringBuffer();
		if(validators != null){
			for(PasswordValidator validator : validators) {
				if(!validator.isValid(password, user)){
					sb.append(validator.getMessage()).append(" \n");
					ret=false;
				}
			}
		}
		message = sb.toString();
		return ret;
		
	}
	public String getMessage(){
		return message;
	}

	public List<PasswordValidator> getValidators() {
		return validators;
	}

	public void setValidators(List<PasswordValidator> validators) {
		this.validators = validators;
	}

}
