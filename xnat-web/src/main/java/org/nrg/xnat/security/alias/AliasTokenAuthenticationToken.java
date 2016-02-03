/*
 * org.nrg.xnat.security.alias.AliasTokenAuthenticationToken
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.security.alias;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class AliasTokenAuthenticationToken extends UsernamePasswordAuthenticationToken{
	public AliasTokenAuthenticationToken(Object principal, Object credentials) {
		super(principal, credentials);
        _principal = principal;
        _credentials = credentials;
        _alias = (String) principal;
        _secret = (Long) credentials;
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

    public long getSecret() {
        return _secret;
    }

    @Override
	public String toString(){
		return getPrincipal().toString();
	}

    private Object _principal;
    private Object _credentials;
    private String _alias;
    private long _secret;
}
