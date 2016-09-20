/*
 * core: org.nrg.xdat.security.user.exceptions.PasswordAuthenticationException
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.user.exceptions;

public class PasswordAuthenticationException extends FailedLoginException {
    public PasswordAuthenticationException(String login) {
        super("Invalid Login and/or Password", login);
    }
}