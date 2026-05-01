/*
 * spawner: org.nrg.xnat.spawner.exceptions.InvalidElementIdException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.exceptions;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NO_CONTENT, reason = "An element ID that doesn't exist was requested.")
@Getter
@Accessors(prefix = "_")
public class InvalidElementIdException extends RuntimeException {
    public InvalidElementIdException(final String namespace, final String invalidElementId) {
        super("An element ID that doesn't exist was requested: " + namespace + "/" + invalidElementId);
        _namespace = namespace;
        _invalidElementId = invalidElementId;
    }

    private final String _namespace;
    private final String _invalidElementId;
}
