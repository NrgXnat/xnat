/*
 * org.nrg.xnat.restlet.resources.ProjectArchive
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.resources;

import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

public class ProjectArchive extends ItemResource {
	final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ProjectArchive.class);
	XnatProjectdata proj=null;
	
	public ProjectArchive(Context context, Request request, Response response) {
		super(context, request, response);
		
		String pID= (String)getParameter(request,"PROJECT_ID");
		if(pID!=null){
			proj = XnatProjectdata.getProjectByIDorAlias(pID, user, false);
		}
		
		if(proj!=null){
			this.getVariants().add(new Variant(MediaType.TEXT_XML));				
		}else{
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Unable to find project '"+ pID + "'");
			return;
		}
	}
	

	@Override
	public Representation getRepresentation(Variant variant) {	
		MediaType mt = overrideVariant(variant);
		
		if(proj!=null){
				ArcProject arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(proj.getId());
	        	return this.representItem(arcProject.getItem(),MediaType.TEXT_XML);
		}else{
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Unable to find the specified scan.");
		}

		return null;

	}
}