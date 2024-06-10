package org.nrg.xnat.eventservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public class SubscriptionAccessException extends Exception {
    public SubscriptionAccessException(final String message) {
        super(message);
    }
    public SubscriptionAccessException(final String message, final Throwable cause) { super(message, cause); }
    public SubscriptionAccessException(final Throwable cause) {
        super(cause);
    }
}

