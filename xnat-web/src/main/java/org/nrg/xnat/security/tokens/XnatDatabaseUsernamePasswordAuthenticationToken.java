/*
 * web: org.nrg.xnat.security.tokens.XnatDatabaseUsernamePasswordAuthenticationToken
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
