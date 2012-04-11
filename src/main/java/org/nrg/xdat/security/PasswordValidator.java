package org.nrg.xdat.security;

public interface PasswordValidator {
	
	public boolean isValid(String password);
	public String getMessage();
}
