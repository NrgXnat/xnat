/*
 * core: org.nrg.xdat.security.user.exceptions.PasswordComplexityException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.user.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "The specified password did not meet the complexity requirements configured for the site.")
public class PasswordComplexityException extends Exception {
    public PasswordComplexityException(String message) {
        super(message);
    }
}
