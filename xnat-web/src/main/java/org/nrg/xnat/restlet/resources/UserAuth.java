/*
 * org.nrg.xnat.restlet.resources.UserAuth
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.resources;

import org.nrg.xft.security.UserI;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

public class UserAuth extends SecureResource {
	
	public UserAuth(Context context, Request request, Response response) {
		super(context, request, response);
		
		getVariants().add(new Variant(MediaType.TEXT_PLAIN));
	}

	@Override
	public Representation represent(Variant variant) throws ResourceException {
		return userAuthRepresentation();
	}

	private Representation userAuthRepresentation() {
		UserI loggedInUser = (UserI) getRequest().getAttributes().get(USER_ATTRIBUTE);
		return new StringRepresentation(String.format("User '%s' is logged in.", loggedInUser.getUsername()),
				MediaType.TEXT_PLAIN);
	}

	@Override
	public boolean allowGet() {
		return true;
	}
	
	@Override
	public boolean allowDelete() {
		return false;
	}

	@Override
	public boolean allowPost() {
		return false;
	}

	@Override
	public boolean allowPut() {
		return false;
	}
}
