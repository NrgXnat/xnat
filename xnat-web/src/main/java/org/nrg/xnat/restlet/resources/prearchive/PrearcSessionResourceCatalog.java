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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.bean.XnatImagescandataBean;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.helpers.prearchive.PrearcTableBuilder;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.restlet.representations.ZipRepresentation;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
			} else {
				File resDir = new File(new File(info.sessionDIR,"RESOURCES"), resourceId);
				if (!resDir.exists()){
					throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND, "Unknown resource " + resourceId, new Exception());
				} else {
					try {
						FileUtils.MoveToCache(resDir);
					} catch (Exception e) {
						logger.error("",e);
						PrearcUtils.log(project,timestamp,session, e);
						throw new ServerException(Status.SERVER_ERROR_INTERNAL,"Failed to delete files.",e);
					}

					info.session.setResources_resource((ArrayList) resources);

					try (FileWriter fw = new FileWriter(info.sessionXML)) {
						info.session.toXML(fw);

						PrearcUtils.log(project, timestamp, session, new Exception("Deleted resource " + resourceId));
						this.getResponse().setStatus(Status.SUCCESS_OK);
					} catch (Exception e) {
						logger.error("Failed to update session xml", e);
						PrearcUtils.log(project, timestamp, session, e);
						throw new ServerException(Status.SERVER_ERROR_INTERNAL, "Failed to update session xml.", e);
					}
				}
			}
		} catch (ActionException e) {
			setResponseStatus(e);
		}
	}
	
	@Override
	public Representation represent(final Variant variant) throws ResourceException {
		return getRepresentation(variant, "RESOURCES", resourceId);
	}
}
