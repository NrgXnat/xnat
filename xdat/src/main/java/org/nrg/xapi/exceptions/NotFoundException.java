/*
 * web: org.nrg.xapi.exceptions.NotFoundException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends XapiException {
    public NotFoundException(final String msg) {
        super(HttpStatus.NOT_FOUND, msg);
    }

    public NotFoundException(final String type, final String name) {
        super(HttpStatus.NOT_FOUND, "No resource of type " + type + " with the ID or name " + name + " exists.");
    }

    public NotFoundException(final String type, final long id) {
        super(HttpStatus.NOT_FOUND, "No resource of type " + type + " with the ID " + id + " exists.");
    }
}
