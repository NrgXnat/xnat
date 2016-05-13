package org.nrg.xapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InitializationException extends Exception {
    public InitializationException(final Throwable throwable) {
        super(throwable);
    }
}
