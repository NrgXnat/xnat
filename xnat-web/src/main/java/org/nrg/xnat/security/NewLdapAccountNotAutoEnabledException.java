/*
 * web: org.nrg.xnat.security.NewLdapAccountNotAutoEnabledException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import org.nrg.xft.security.UserI;
import org.springframework.security.core.AuthenticationException;

public class NewLdapAccountNotAutoEnabledException extends AuthenticationException 
{
	private static final long serialVersionUID = 1L;
	
	private UserI userDetails;

	public NewLdapAccountNotAutoEnabledException(String msg) 
	{
		super(msg);
	}

	public NewLdapAccountNotAutoEnabledException(String msg, UserI userDetails) 
	{
		super(msg);
		setUserDetails(userDetails);
	}

	public UserI getUserDetails() {
		return userDetails;
	}

	public void setUserDetails(UserI userDetails) {
		this.userDetails = userDetails;
	}
}
