package org.nrg.xapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictedStateException extends XapiException {
    private static final long serialVersionUID = 5335077123732430240L;

    public ConflictedStateException(final String message) {
        super(HttpStatus.CONFLICT, message);
    }

    public ConflictedStateException(final HttpStatus status, final Throwable throwable) {
        super(HttpStatus.CONFLICT, throwable);
    }

    public ConflictedStateException(final HttpStatus status, final String message, final Throwable throwable) {
        super(HttpStatus.CONFLICT, message, throwable);
    }
}
