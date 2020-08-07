package org.nrg.xnat.eventservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FAILED_DEPENDENCY)
public class UnsatisfiedDependencyException extends Exception {
    public UnsatisfiedDependencyException(final String message) {
        super(message);
    }

    public UnsatisfiedDependencyException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UnsatisfiedDependencyException(final Throwable cause) {
        super(cause);
    }
}

