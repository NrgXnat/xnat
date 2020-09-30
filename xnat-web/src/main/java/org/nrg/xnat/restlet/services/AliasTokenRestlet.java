/*
 * web: org.nrg.xnat.restlet.services.AliasTokenRestlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.services;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.nrg.framework.exceptions.NotFoundException;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class AliasTokenRestlet extends SecureResource {
    private static final String PARAM_OPERATION = "OPERATION";
    private static final String PARAM_USERNAME  = "USERNAME";
    private static final String PARAM_TOKEN     = "TOKEN";
    private static final String PARAM_SECRET    = "SECRET";
    private static final String OP_SHOW         = "show";
    private static final String OP_ISSUE        = "issue";
    private static final String OP_VALIDATE     = "validate";
    private static final String OP_INVALIDATE   = "invalidate";

    public AliasTokenRestlet(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));

        _serializer = XDAT.getSerializerService();
        if (_serializer == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_FAILED_DEPENDENCY, "Serializer service was not properly initialized.");
            throw new ResourceException(Status.CLIENT_ERROR_FAILED_DEPENDENCY, "ERROR: Serializer service was not properly initialized.");
        }
        _service = XDAT.getContextService().getBean(AliasTokenService.class);
        if (_service == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_FAILED_DEPENDENCY, "Alias token service was not properly initialized.");
            throw new ResourceException(Status.CLIENT_ERROR_FAILED_DEPENDENCY, "ERROR: Alias token service was not properly initialized.");
        }

        final String     tokenId = (String) getRequest().getAttributes().get(PARAM_TOKEN);
        final AliasToken token;
        if (StringUtils.isBlank(tokenId)) {
            _token = null;
            token = null;
        } else if (AliasToken.isAliasFormat(tokenId)) {
            token = _service.locateToken(tokenId);
            if (token == null) {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Can't find an alias token with the alias " + tokenId);
            }
            _token = tokenId;
        } else if (NumberUtils.isCreatable(tokenId)) {
            try {
                token = _service.get(NumberUtils.toLong(tokenId));
                _token = token.getAlias();
            } catch (NotFoundException e) {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Can't find an alias token with the ID " + tokenId);
            }
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Can't find an alias token with the ID " + tokenId);
        }

        final UserI user = getUser();
        _username = StringUtils.defaultIfBlank((String) getRequest().getAttributes().get(PARAM_USERNAME), user.getUsername());
        if (StringUtils.isBlank(_username) || StringUtils.equalsIgnoreCase(_username, "guest")) {
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "The guest user can't request alias tokens");
        }
        final boolean siteAdmin = Roles.isSiteAdmin(user);
        if ((!StringUtils.equalsIgnoreCase(_username, user.getUsername()) || token != null && !StringUtils.equalsIgnoreCase(token.getXdatUserId(), user.getUsername())) && !siteAdmin) {
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "Non-admin users can only request operations on alias tokens for their own account");
        }
        _operation = (String) getRequest().getAttributes().get(PARAM_OPERATION);
        _secret = (String) getRequest().getAttributes().get(PARAM_SECRET);
    }

    @Override
    public Representation represent() throws ResourceException {
        final UserI user = getUser();

        final boolean hasUsername = StringUtils.isNotBlank(_username);
        if (hasUsername && !StringUtils.equals(_username, user.getUsername()) && !Roles.isSiteAdmin(user)) {
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "Only admins can work with alias tokens for other users.");
        }

        switch (_operation) {
            case OP_ISSUE:
                try {
                    final AliasToken token = hasUsername ? _service.issueTokenForUser(_username) : _service.issueTokenForUser(user);
                    return new StringRepresentation(_serializer.toJson(token), MediaType.APPLICATION_JSON);
                } catch (Exception exception) {
                    throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "An error occurred retrieving the user: " + _username, exception);
                }

            case OP_SHOW:
                final String username = hasUsername ? _username : user.getUsername();
                if (StringUtils.isBlank(username)) {
                    throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No user found.");
                }
                final List<AliasToken> tokens = _service.findTokensForUser(username);
                try {
                    return new StringRepresentation(_serializer.toJson(ObjectUtils.defaultIfNull(tokens, Collections.<AliasToken>emptyList())), MediaType.APPLICATION_JSON);
                } catch (IOException e) {
                    throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "An error occurred returning alias tokens", e);
                }

            case OP_VALIDATE:
                if (StringUtils.isBlank(_token) || StringUtils.isBlank(_secret)) {
                    throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, "You must specify both token and secret to validate a token.");
                }
                try {
                    final HashMap<String, String> results = new HashMap<>();
                    results.put("valid", _service.validateToken(_token, _secret));
                    return new StringRepresentation(_serializer.toJson(results), MediaType.APPLICATION_JSON);
                } catch (IOException exception) {
                    throw new ResourceException(Status.SERVER_ERROR_INTERNAL, exception.toString());
                }

            case OP_INVALIDATE:
                _service.invalidateToken(_token);
                return new StringRepresentation("{\"result\": \"OK\"}", MediaType.APPLICATION_JSON);

            default:
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown operation: " + _operation);
        }
    }

    @Override
    public Representation represent(final Variant variant) throws ResourceException {
        return represent();
    }

    private final SerializerService _serializer;
    private final AliasTokenService _service;
    private final String            _operation;
    private final String            _username;
    private final String            _token;
    private final String            _secret;
}
