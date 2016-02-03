/*
 * org.nrg.xnat.security.tokens.XnatDatabaseUsernamePasswordAuthenticationToken
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.security.tokens;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class XnatDatabaseUsernamePasswordAuthenticationToken extends UsernamePasswordAuthenticationToken{

	public XnatDatabaseUsernamePasswordAuthenticationToken(Object principal,
			Object credentials) {
		super(principal, credentials);
	}

	public String toString(){
		if(getPrincipal()!=null)
			return getPrincipal().toString();
		else
			return "";
	}
}
