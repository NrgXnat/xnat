/*
 * spawner: org.nrg.xnat.spawner.exceptions.UnknownValidationSetException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NO_CONTENT)
public class UnknownValidationSetException extends Throwable {
    public UnknownValidationSetException(final String set, final String id) {
        super("Unknown validation set requested: " + set + "." + id);
    }
}
