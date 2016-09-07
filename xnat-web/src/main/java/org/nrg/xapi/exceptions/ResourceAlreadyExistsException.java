package org.nrg.xapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceAlreadyExistsException extends ApiException {
    public ResourceAlreadyExistsException(final String type, final String name) {
        super(HttpStatus.CONFLICT.value(), "The resource of type " + type + " with the name " + name + " already exists.");
    }
}
