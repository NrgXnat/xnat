/*
 * web: org.nrg.xnat.restlet.resources.prearchive.PrearcScanResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * 
 */
package org.nrg.xnat.restlet.resources.prearchive;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tolsen01
 *
 */
public class PrearcSessionResourceCatalog extends PrearcSessionResourceA {
	private static final String RESOURCE_ID = "RESOURCE_ID";

	static Logger logger = Logger.getLogger(PrearcSessionResourceCatalog.class);

	protected final String resourceId;

	public PrearcSessionResourceCatalog(Context context, Request request,
                                        Response response) {
		super(context, request, response);
		resourceId = (String)getParameter(request,RESOURCE_ID);
	}

	@Override
	public boolean allowDelete() {
		return true;
	}
	
	@Override
	public boolean allowGet() {
		return true;
	}
	
	@Override
	public void handleDelete() {
		final PrearcInfo info;
		try {
			info = retrieveSessionBean();
			List<XnatAbstractresourceI> resources = info.session.getResources_resource();
			XnatAbstractresourceI resource = null;
			for (XnatAbstractresourceI r : resources){
				if(StringUtils.equals(r.getLabel(), resourceId)){
					resource = r;
					resources.remove(r);
					break;
				}
			}

			if (resource == null){
				throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND, "Unknown resource " + resourceId, new Exception());
			}

			File resDir = new File(new File(info.sessionDIR,"RESOURCES"), resourceId);
			if (!resDir.exists()){
				throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND, "No resource " + resourceId, new Exception());
			}

			try {
				FileUtils.deleteDirectory(resDir);
			} catch (Exception e) {
				logger.error("",e);
				PrearcUtils.log(project,timestamp,session, e);
				throw new ServerException(Status.SERVER_ERROR_INTERNAL,"Failed to delete files.",e);
			}
			saveSessionBean(info);
			PrearcUtils.log(project, timestamp, session, new Exception("Deleted resource " + resourceId));
		} catch (ActionException e) {
			setResponseStatus(e);
		}
	}
	
	@Override
	public Representation represent(final Variant variant) throws ResourceException {
		return getRepresentation(variant, "RESOURCES", resourceId);
	}
}
