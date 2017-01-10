/*
 * web: org.nrg.xnat.restlet.representations.RESTLoginRepresentation
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.representations;

import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.apache.turbine.util.TurbineException;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;
import org.restlet.data.MediaType;
import org.restlet.data.Request;

public class RESTLoginRepresentation extends TurbineScreenRepresentation {
	static org.apache.log4j.Logger logger = Logger.getLogger(RESTLoginRepresentation.class);
	XFTItem item = null;
	boolean includeSchemaLocations=true;

	public RESTLoginRepresentation(MediaType mt, Request _request, UserI _user) throws TurbineException {
		super(mt,_request,_user,new Hashtable<String,Object>());	
		
    	data.getParameters().add("rest_uri", request.getOriginalRef().toString());
	}

	@Override
	public String getScreen() {
		return "Login.vm";
	}
}
