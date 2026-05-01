/*
 * web: org.nrg.xapi.exceptions.NotModifiedException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2020, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_MODIFIED)
public class NotModifiedException extends XapiException {
    public NotModifiedException(final String message) {
        super(HttpStatus.NOT_MODIFIED, message);
    }
}
