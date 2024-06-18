/*
 * core: org.nrg.xdat.security.user.exceptions.UserInitException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.user.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UserInitException extends Exception {
    public UserInitException(final String message) {
        super(message);
    }

    public UserInitException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
