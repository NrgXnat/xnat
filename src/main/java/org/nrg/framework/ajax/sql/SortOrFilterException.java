// Developer: Kate Alpert <kate@radiologics.com>

package org.nrg.framework.ajax.sql;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SortOrFilterException extends Exception {
    public SortOrFilterException() {
        super();
    }

    public SortOrFilterException(final String message) {
        super(message);
    }

    public SortOrFilterException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SortOrFilterException(final Throwable cause) {
        super(cause);
    }
}
