package org.nrg.xnat.eventservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FAILED_DEPENDENCY)
public class SubscriptionValidationException extends Exception {
    public SubscriptionValidationException(final String message) {
        super(message);
    }
    public SubscriptionValidationException(final String message, final Throwable cause) { super(message, cause); }

    public SubscriptionValidationException(final Throwable cause) {
        super(cause);
    }
}

