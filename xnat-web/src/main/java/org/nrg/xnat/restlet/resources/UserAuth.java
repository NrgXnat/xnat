/*
 * web: org.nrg.xnat.restlet.resources.UserAuth
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.apache.commons.lang3.BooleanUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xft.security.UserI;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

public class UserAuth extends SecureResource {
    public UserAuth(Context context, Request request, Response response) {
        super(context, request, response);

        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        setModifiable(false);
        setReadable(true);

        _includeXnatCsrfToken = BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean(getQueryVariable("CSRF")), false);
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        final UserI loggedInUser = XDAT.getUserDetails();
        if (loggedInUser == null) {
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED);
        }
        final String message = String.format(LOGGED_IN, loggedInUser.getUsername()) + (_includeXnatCsrfToken ? "; XNAT_CSRF=" + getHttpSession().getAttribute("XNAT_CSRF") : "");
        return new StringRepresentation(message, MediaType.TEXT_PLAIN);
    }

    private static final String LOGGED_IN = "User '%s' is logged in";

    private final boolean _includeXnatCsrfToken;
}
