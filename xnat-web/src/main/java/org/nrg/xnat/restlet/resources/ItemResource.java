/*
 * org.nrg.xnat.restlet.resources.ItemResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.resources;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public abstract class ItemResource extends SecureResource {

	public ItemResource(Context context, Request request, Response response) {
		super(context, request, response);
		
	}

}
