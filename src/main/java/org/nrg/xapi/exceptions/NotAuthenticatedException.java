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
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@SuppressWarnings({"Duplicates", "unused"})
@ResponseStatus(UNAUTHORIZED)
@Getter
@Accessors(prefix = "_")
public class NotAuthenticatedException extends XapiException {
    public NotAuthenticatedException(final String request) {
        super(UNAUTHORIZED, request);
        _request = request;
        _project = null;
    }

    public NotAuthenticatedException(final String request, final String resource) {
        super(UNAUTHORIZED, request);
        _request = request;
        _project = null;
        _resources.add(resource);
    }

    public NotAuthenticatedException(final String request, final List<String> resources) {
        super(UNAUTHORIZED, request);
        _request = request;
        _project = null;
        _resources.addAll(resources);
    }

    public NotAuthenticatedException(final String request, final Set<String> resources) {
        super(UNAUTHORIZED, request);
        _request = request;
        _project = null;
        _resources.addAll(resources);
    }

    public NotAuthenticatedException(final String request, final String project, final List<String> resources) {
        super(UNAUTHORIZED, request);
        _request = request;
        _project = project;
        _resources.addAll(resources);
    }

    public NotAuthenticatedException(final String request, final String project, final Set<String> resources) {
        super(UNAUTHORIZED, request);
        _request = request;
        _project = project;
        _resources.addAll(resources);
    }

    @Override
    public String getMessage() {
        if (StringUtils.isNotBlank(_project)) {
            switch (_resources.size()) {
                case 0:
                    return String.format(MESSAGE_PROJECT, _request, _project);

                case 1:
                    return String.format(MESSAGE_PROJECT_RESOURCE, _request, _project, getResource());

                default:
                    return String.format(MESSAGE_PROJECT_RESOURCES, _request, _project, getResource());
            }
        } else {
            switch (_resources.size()) {
                case 0:
                    return String.format(MESSAGE, _request);

                case 1:
                    return String.format(MESSAGE_RESOURCE, _request, getResource());

                default:
                    return String.format(MESSAGE_RESOURCES, _request, getResource());
            }
        }
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

    private static final String MESSAGE_PROJECT           = "Request %s: An unauthenticated user tried to access the restricted project %s";
    private static final String MESSAGE_PROJECT_RESOURCE  = "Request %s: An unauthenticated user tried to access the restricted project %s on the resource %s";
    private static final String MESSAGE_PROJECT_RESOURCES = "Request %s: An unauthenticated user tried to access the restricted project %s on the resources: %s";
    private static final String MESSAGE                   = "Request %s: An unauthenticated user has insufficient privileges for the requested operation.";
    private static final String MESSAGE_RESOURCE          = "Request %s: An unauthenticated user has insufficient privileges for the requested operation on the resource %s.";
    private static final String MESSAGE_RESOURCES         = "Request %s: An unauthenticated user has insufficient privileges for the requested operation on the resources: %s";

    private final String _request;
    private final String _project;
    private final Set<String> _resources = new HashSet<>();
}
