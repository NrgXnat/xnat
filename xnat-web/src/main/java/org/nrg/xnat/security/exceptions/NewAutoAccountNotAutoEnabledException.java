/*
 * web: NewAutoAccountNotAutoEnabledException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xnat.security.exceptions;

import org.nrg.xft.security.UserI;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;

/**
 * This exception indicates that an auto-created account&mdash;usually created to provide a controlling
 * account on the XNAT system for a user logging in for the first time using an {@link XnatAuthenticationProvider
 * external authentication provider} such as LDAP, OAuth, and so on&mdash;was successfully created but
 * couldn't be enabled automatically, so that the user can't be logged in. This indicates that the actual
 * <i>authentication</i> was successful but that the user won't be able to access the system through the
 * newly created system account until it's enabled somehow, e.g. manual activation by an administrator,
 * completion of an email verification process, etc.
 */
public class NewAutoAccountNotAutoEnabledException extends AuthenticationException {
    public NewAutoAccountNotAutoEnabledException(final String message, final UserI user) {
        super(message);
        setUser(user);
    }

    public UserI getUser() {
        return _user;
    }

    public void setUser(final UserI user) {
        _user = user;
    }

    private UserI _user;
}
