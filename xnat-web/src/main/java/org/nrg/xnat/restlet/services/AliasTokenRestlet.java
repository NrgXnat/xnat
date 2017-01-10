/*
 * web: org.nrg.xnat.restlet.services.AliasTokenRestlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.services;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import java.io.IOException;
import java.util.HashMap;

public class AliasTokenRestlet extends SecureResource {
    private static final String PARAM_OPERATION = "OPERATION";
    private static final String PARAM_USERNAME  = "USERNAME";
    private static final String PARAM_TOKEN     = "TOKEN";
    private static final String PARAM_SECRET    = "SECRET";
    private static final String OP_ISSUE        = "issue";
    private static final String OP_VALIDATE     = "validate";
    private static final String OP_INVALIDATE   = "invalidate";

    public AliasTokenRestlet(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        _operation = (String) getRequest().getAttributes().get(PARAM_OPERATION);
        _username = (String) getRequest().getAttributes().get(PARAM_USERNAME);
        _token = (String) getRequest().getAttributes().get(PARAM_TOKEN);
        _secret = (String) getRequest().getAttributes().get(PARAM_SECRET);

        _serializer = XDAT.getContextService().getBean(SerializerService.class);
        if (null == _serializer) {
            getResponse().setStatus(Status.CLIENT_ERROR_FAILED_DEPENDENCY, "Serializer service was not properly initialized.");
            throw new ResourceException(Status.CLIENT_ERROR_FAILED_DEPENDENCY, "ERROR: Serializer service was not properly initialized.");
        }
    }

    @Override
    public Representation represent() throws ResourceException {
        if (OP_ISSUE.equals(_operation)) {
            final UserI user = getUser();
            if (!StringUtils.isBlank(_username) && !Roles.isSiteAdmin(user)) {
                throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "Only admins can create proxy tokens.");
            }
            try {
                AliasToken token = StringUtils.isBlank(_username) ? getService().issueTokenForUser(user) : getService().issueTokenForUser(_username);
                return new StringRepresentation(_serializer.toJson(token));
            } catch (Exception exception) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "An error occurred retrieving the user: " + _username, exception);
            }
        } else if (OP_VALIDATE.equals(_operation)) {
            if (StringUtils.isBlank(_token) || StringUtils.isBlank(_secret)) {
                throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, "You must specify both token and secret to validate a token.");
            }
            try {
                final HashMap<String, String> results = new HashMap<>();
                results.put("valid", getService().validateToken(_token, _secret));
                return new StringRepresentation(_serializer.toJson(results));
            } catch (IOException exception) {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, exception.toString());
            }
        }else if (OP_INVALIDATE.equals(_operation)) {
            getService().invalidateToken(_token);
            return new StringRepresentation("{\"result\": \"OK\"}");
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown operation: " + _operation);
        }
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        return represent();
    }

    private AliasTokenService getService() {
        if (_service == null) {
            _service = XDAT.getContextService().getBean(AliasTokenService.class);
        }
        return _service;
    }

    private final SerializerService _serializer;
    private       AliasTokenService _service;
    private       String            _operation;
    private final String            _username;
    private final String            _token;
    private final String              _secret;
}
