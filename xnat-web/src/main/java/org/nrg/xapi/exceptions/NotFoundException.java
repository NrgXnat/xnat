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
public class NotFoundException extends ApiException {
    public NotFoundException(String msg) {
        super(HttpStatus.NOT_FOUND.value(), msg);
    }
}
