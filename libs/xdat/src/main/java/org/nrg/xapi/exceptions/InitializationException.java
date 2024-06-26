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

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InitializationException extends XapiException {
    public InitializationException(final Throwable throwable) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, throwable);
    }

    public InitializationException(final String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public InitializationException(final String message, final Throwable throwable) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message, throwable);
    }
}
