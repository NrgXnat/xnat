/*
 * web: org.nrg.xnat.restlet.resources.UserSession
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.apache.commons.lang3.BooleanUtils;
import org.nrg.xdat.security.helpers.UserHelper;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;

public class UserSession extends SecureResource {
    public UserSession(final Context context, final Request request, final Response response) {
        super(context, request, response);

        getVariants().add(new Variant(MediaType.TEXT_PLAIN));

        // copy the user from the request into the session
        UserHelper.setUserHelper(getHttpServletRequest(), getUser());

        _includeXnatCsrfToken = BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean(getQueryVariable("CSRF")), false);
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
    public void removeRepresentations() {
        getHttpSession().invalidate();
    }

    @Override
    public void acceptRepresentation(final Representation entity) {
        getResponse().setEntity(sessionIdRepresentation());
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        return sessionIdRepresentation();
    }

    private Representation sessionIdRepresentation() {
        return new StringRepresentation(getHttpSession().getId() + (_includeXnatCsrfToken ? "; XNAT_CSRF=" + getHttpSession().getAttribute("XNAT_CSRF") : ""), MediaType.TEXT_PLAIN);
    }

    private final boolean _includeXnatCsrfToken;
}
