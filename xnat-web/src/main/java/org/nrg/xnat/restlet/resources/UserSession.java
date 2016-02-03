/*
 * org.nrg.xnat.restlet.resources.UserSession
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.resources;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xft.security.UserI;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

public class UserSession extends SecureResource {
    protected UserI user = null;

    public UserSession(Context context, Request request, Response response) {
        super(context, request, response);

        getVariants().add(new Variant(MediaType.TEXT_PLAIN));

        // copy the user from the request into the session
        getHttpSession().setAttribute(USER_ATTRIBUTE, getRequest().getAttributes().get(USER_ATTRIBUTE));

        getHttpSession().setAttribute("userHelper", UserHelper.getUserHelperService(user));

        getHttpSession().setAttribute("XNAT_CSRF", UUID.randomUUID().toString());
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public void removeRepresentations() throws ResourceException {
        getHttpSession().invalidate();
    }

    @Override
    public void acceptRepresentation(Representation entity)
            throws ResourceException {
        getResponse().setEntity(sessionIdRepresentation());
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        return sessionIdRepresentation();
    }

    private Representation sessionIdRepresentation() {
        return new StringRepresentation(getHttpSession().getId(),
                MediaType.TEXT_PLAIN);
    }
}
