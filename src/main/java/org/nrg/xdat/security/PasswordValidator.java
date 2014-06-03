package org.nrg.xdat.security;

import org.nrg.xft.security.UserI;

public interface PasswordValidator {
	
	public boolean isValid(String password, UserI user);
	public String getMessage();
}
