/*
 * web: org.nrg.xnat.restlet.representations.StandardTurbineScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * 
 */
package org.nrg.xnat.restlet.representations;

import java.util.Map;

import org.apache.turbine.util.RunData;
import org.apache.turbine.util.TurbineException;
import org.nrg.xft.security.UserI;
import org.restlet.data.MediaType;
import org.restlet.data.Request;

/**
 * @author tolsen01
 *
 */
public class StandardTurbineScreen extends TurbineScreenRepresentation {
	final String screen;
	/**
	 * @param mediaType
	 * @param request
	 * @param _user
	 * @throws TurbineException
	 */
	public StandardTurbineScreen(MediaType mediaType, Request request,
			UserI _user, String screen, Map<String,Object> data_props) throws TurbineException {
		super(mediaType, request, _user,data_props);
		this.screen=screen;
	}
	
	public RunData getData(){
		return data;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xnat.restlet.representations.TurbineScreenRepresentation#getScreen()
	 */
	@Override
	public String getScreen() {
		return screen;
	}

}
