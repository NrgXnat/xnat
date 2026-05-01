/*
 * spawner: org.nrg.xnat.spawner.exceptions.InvalidValidationValueException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidValidationValueException extends Throwable {
    public InvalidValidationValueException(final String id) {
        super("No value set for element ID: " + id);
    }
}
