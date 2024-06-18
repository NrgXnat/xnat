/*
 * web: org.nrg.xapi.exceptions.ApiException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.exceptions;

import org.springframework.http.HttpStatus;

public class XapiException extends Exception {
    public XapiException(final HttpStatus status, final String message) {
        super(message);
        _status = status;
    }

    public XapiException(final HttpStatus status, final Throwable throwable) {
        super(throwable);
        _status = status;
    }

    public XapiException(final HttpStatus status, final String message, final Throwable throwable) {
        super(message, throwable);
        _status = status;
    }

    public HttpStatus getStatus() {
        return _status;
    }

    private HttpStatus _status;
}
