/*
 * web: org.nrg.xnat.security.alias.AliasTokenAuthenticationToken
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.alias;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class AliasTokenAuthenticationToken extends UsernamePasswordAuthenticationToken{
	public AliasTokenAuthenticationToken(Object principal, Object credentials) {
		super(principal, credentials);
        _principal = principal;
        _credentials = credentials;
        _alias = (String) principal;
        _secret = (String) credentials;
	}

    @Override
    public Object getPrincipal() {
        return _principal;
    }

    public void setPrincipal(Object principal) {
        _principal = principal;
    }

    @Override
    public Object getCredentials() {
        return _credentials;
    }

    public void setCredentials(Object credentials) {
        _credentials = credentials;
    }

    public String getAlias() {
        return _alias;
    }

    public String getSecret() {
        return _secret;
    }

    @Override
	public String toString(){
		return getPrincipal().toString();
	}

    private Object _principal;
    private Object _credentials;
    private String _alias;
    private String _secret;
}
