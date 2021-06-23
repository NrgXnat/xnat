/*
 * core: org.nrg.xdat.security.user.exceptions.UserNotFoundException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.user.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends FailedLoginException {
    public UserNotFoundException(final String username) {
        super("Invalid username and/or password", username);
    }

    public UserNotFoundException(final Integer id) {
        super("Invalid user ID", id.toString());
    }
}
