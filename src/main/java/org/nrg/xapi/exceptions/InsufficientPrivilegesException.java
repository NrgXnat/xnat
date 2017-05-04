/*
 * web: org.nrg.xapi.exceptions.InsufficientPrivilegesException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.exceptions;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class InsufficientPrivilegesException extends XapiException {
    public InsufficientPrivilegesException(final String username) {
        super(HttpStatus.FORBIDDEN, username);
        _username = username;
        _resource = null;
    }

    public InsufficientPrivilegesException(final String username, final String resource) {
        super(HttpStatus.FORBIDDEN, username);
        _username = username;
        _resource = resource;
    }

    @Override
    public String getMessage() {
        return StringUtils.isBlank(_resource)
               ? String.format("The user %s has insufficient privileges for the requested operation.", _username)
               : String.format("The user %s has insufficient privileges for the requested operation on the resource %s.", _username, _resource);
    }

    public String getUsername() {
        return _username;
    }

    public String getResource() {
        return _resource;
    }

    private final String _username;
    private final String _resource;
}
