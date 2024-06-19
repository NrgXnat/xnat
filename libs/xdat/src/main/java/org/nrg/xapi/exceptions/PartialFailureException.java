/*
 * web: org.nrg.xapi.exceptions.InitializationException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.ACCEPTED)
public class PartialFailureException extends XapiException {
    public PartialFailureException(final Throwable throwable) {
        super(HttpStatus.ACCEPTED, throwable);
    }

    public PartialFailureException(final String message) {
        super(HttpStatus.ACCEPTED, message);
    }

    public PartialFailureException(final String message, final Throwable throwable) {
        super(HttpStatus.ACCEPTED, message, throwable);
    }
}