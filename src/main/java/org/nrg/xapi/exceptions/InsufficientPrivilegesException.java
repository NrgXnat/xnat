/*
 * web: org.nrg.xapi.exceptions.InsufficientPrivilegesException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.exceptions;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class InsufficientPrivilegesException extends XapiException {
    public InsufficientPrivilegesException(final String username) {
        super(HttpStatus.FORBIDDEN, username);
        _username = username;
        _project = null;
    }

    public InsufficientPrivilegesException(final String username, final String resource) {
        super(HttpStatus.FORBIDDEN, username);
        _username = username;
        _project = null;
        _resources.add(resource);
    }

    public InsufficientPrivilegesException(final String username, final List<String> resources) {
        super(HttpStatus.FORBIDDEN, username);
        _username = username;
        _project = null;
        _resources.addAll(resources);
    }

    public InsufficientPrivilegesException(final String username, final Set<String> resources) {
        super(HttpStatus.FORBIDDEN, username);
        _username = username;
        _project = null;
        _resources.addAll(resources);
    }

    public InsufficientPrivilegesException(final String username, final String project, final List<String> resources) {
        super(HttpStatus.FORBIDDEN, username);
        _username = username;
        _project = project;
        _resources.addAll(resources);
    }

    public InsufficientPrivilegesException(final String username, final String project, final Set<String> resources) {
        super(HttpStatus.FORBIDDEN, username);
        _username = username;
        _project = project;
        _resources.addAll(resources);
    }

    @Override
    public String getMessage() {
        if (StringUtils.isNotBlank(_project)) {
            switch (_resources.size()) {
                case 0:
                    return String.format(MESSAGE_USERNAME_PROJECT, _username, _project);

                case 1:
                    return String.format(MESSAGE_USERNAME_PROJECT_RESOURCE, _username, _project, getResource());

                default:
                    return String.format(MESSAGE_USERNAME_PROJECT_RESOURCES, _username, _project, getResource());
            }
        } else {
            switch (_resources.size()) {
                case 0:
                    return String.format(MESSAGE_USERNAME, _username);

                case 1:
                    return String.format(MESSAGE_USERNAME_RESOURCE, _username, getResource());

                default:
                    return String.format(MESSAGE_USERNAME_RESOURCES, _username, getResource());
            }
        }
    }

    public String getUsername() {
        return _username;
    }

    public String getResource() {
        switch (_resources.size()) {
            case 0:
                return "";

            case 1:
                return _resources.iterator().next();

            default:
                return Joiner.on(", ").join(_resources);
        }
    }

    public Set<String> getResources() {
        return _resources;
    }

    private static final String MESSAGE_USERNAME_PROJECT           = "The user %s has insufficient privileges for the requested operation on project %s.";
    private static final String MESSAGE_USERNAME_PROJECT_RESOURCE  = "The user %s has insufficient privileges for the requested operation in project %s on the resource %s.";
    private static final String MESSAGE_USERNAME_PROJECT_RESOURCES = "The user %s has insufficient privileges for the requested operation in project %s on the resources: %s";
    private static final String MESSAGE_USERNAME                   = "The user %s has insufficient privileges for the requested operation.";
    private static final String MESSAGE_USERNAME_RESOURCE          = "The user %s has insufficient privileges for the requested operation on the resource %s.";
    private static final String MESSAGE_USERNAME_RESOURCES         = "The user %s has insufficient privileges for the requested operation on the resources: %s";

    private final String _username;
    private final String _project;
    private final Set<String> _resources = new HashSet<>();
}
