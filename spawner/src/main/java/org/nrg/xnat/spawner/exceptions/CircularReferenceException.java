/*
 * spawner: org.nrg.xnat.spawner.exceptions.CircularReferenceException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.exceptions;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.nrg.framework.exceptions.NrgServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "A circular reference was found in the requested element.")
@Getter
@Accessors(prefix = "_")
public class CircularReferenceException extends NrgServiceException {
    public CircularReferenceException(final String elementId) {
        super("A circular reference was found in the requested element: " + elementId);
        _elementId = elementId;
    }

    private final String _elementId;
}
