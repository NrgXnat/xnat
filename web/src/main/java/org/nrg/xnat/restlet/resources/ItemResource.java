/*
 * web: org.nrg.xnat.restlet.resources.ItemResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
