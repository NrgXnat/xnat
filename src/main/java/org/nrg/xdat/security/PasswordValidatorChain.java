package org.nrg.xdat.security;

import java.util.ArrayList;
import java.util.List;

public class PasswordValidatorChain implements PasswordValidator {
	List<PasswordValidator> validators;
	String message;
	
	//if there are no validators, just return true;
	@Override
	public boolean isValid(String password, XDATUser user){
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
