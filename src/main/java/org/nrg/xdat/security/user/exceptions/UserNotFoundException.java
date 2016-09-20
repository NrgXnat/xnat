/*
 * core: org.nrg.xdat.security.user.exceptions.UserNotFoundException
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.user.exceptions;

public class UserNotFoundException extends FailedLoginException {
    public UserNotFoundException(String login) {
        super("Invalid Login and/or Password", login);
    }
    public UserNotFoundException(Integer id) {
        super("Invalid User id", id.toString());
    }
}