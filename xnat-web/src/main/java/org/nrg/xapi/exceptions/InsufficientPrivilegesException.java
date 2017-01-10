/*
 * web: org.nrg.xapi.exceptions.InsufficientPrivilegesException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientPrivilegesException extends ApiException {
    public InsufficientPrivilegesException(final String username) {
        super(HttpStatus.BAD_REQUEST.value(), username);
    }

    @Override
    public String getMessage() {
        return String.format("The user %s has insufficient privileges for the requested operation.", super.getMessage());
    }
}
