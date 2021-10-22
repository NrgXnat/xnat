/*
 * web: org.nrg.xnat.restlet.resources.prearchive.PrearcSessionResourceA
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.prearchive;

import org.apache.log4j.Logger;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xnat.helpers.prearchive.PrearcTableBuilder;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.restlet.representations.ZipRepresentation;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.io.File;
import java.io.FileWriter;

public abstract class PrearcSessionResourceA extends SecureResource {
	static Logger logger = Logger.getLogger(PrearcSessionResourceA.class);

	protected static final String PROJECT_ATTR = "PROJECT_ID";
	protected static final String SESSION_TIMESTAMP = "SESSION_TIMESTAMP";
	protected static final String SESSION_LABEL = "SESSION_LABEL";
	protected final String project;
	protected final String timestamp;
	protected final String session;


	public PrearcSessionResourceA(Context context, Request request,
			Response response) {
		super(context, request, response);
		
		project = (String)getParameter(request,PROJECT_ATTR);
		timestamp = (String)getParameter(request,SESSION_TIMESTAMP);
		session = (String)getParameter(request,SESSION_LABEL);
		
		this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
		this.getVariants().add(new Variant(MediaType.TEXT_HTML));
		this.getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	
	public class PrearcInfo{
		public final File sessionDIR;
		public final File sessionXML;
		public final XnatImagesessiondataBean session;
		
		public PrearcInfo(final File dir, final File xml, final XnatImagesessiondataBean session){
			this.sessionDIR=dir;
			this.sessionXML=xml;
			this.session=session;
		}
	}

	protected PrearcInfo retrieveSessionBean() throws ActionException {
		File sessionDIR;
		File srcXML;
		try {
			sessionDIR = PrearcUtils.getPrearcSessionDir(getUser(), project, timestamp, session,false);
			srcXML=new File(sessionDIR.getAbsolutePath()+".xml");
		} catch (InvalidPermissionException e) {
			logger.error("",e);
			throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN,e);
		} catch (Exception e) {
			logger.error("",e);
			throw new ServerException(e);
		}
		
		if(!srcXML.exists()){
			throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND,"Unable to locate prearc resource.",new Exception());
		}
		
		try {
			return new PrearcInfo(sessionDIR,srcXML,PrearcTableBuilder.parseSession(srcXML));
		} catch (Exception e) {
			logger.error("",e);
			throw new ServerException(e);
		}
	}

	protected void saveSessionBean(PrearcInfo info) throws ServerException {
		try (FileWriter fw = new FileWriter(info.sessionXML)) {
			info.session.toXML(fw);
			this.getResponse().setStatus(Status.SUCCESS_OK);
		} catch (Exception e) {
			logger.error("Failed to update session xml", e);
			PrearcUtils.log(project, timestamp, session, e);
			throw new ServerException(Status.SERVER_ERROR_INTERNAL, "Failed to update session xml.", e);
		}
	}

	protected Representation getRepresentation(Variant variant, String type, String id) {
		final MediaType mt=overrideVariant(variant);
		if (MediaType.APPLICATION_GNU_ZIP.equals(mt) || MediaType.APPLICATION_ZIP.equals(mt)) {
			try {
				final File sessionDIR;
				final File srcXML;
				try {
					sessionDIR = PrearcUtils.getPrearcSessionDir(getUser(), project, timestamp, session,false);
					srcXML=new File(sessionDIR.getAbsolutePath()+".xml");
				} catch (InvalidPermissionException e) {
					logger.error("",e);
					throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN,e);
				} catch (Exception e) {
					logger.error("",e);
					throw new ServerException(e);
				}

				if(!srcXML.exists()){
					throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND,"Unable to locate prearc resource.",new Exception());
				}

				File resDir = new File(new File(sessionDIR,type), id);

				final ZipRepresentation zip;
				try{
					zip = new ZipRepresentation(mt, resDir.getName(),identifyCompression(null));
				} catch (ActionException e) {
					logger.error("",e);
					this.setResponseStatus(e);
					return null;
				}
				zip.addFolder(resDir.getName(), resDir);
				return zip;
			} catch (ClientException e) {
				this.getResponse().setStatus(e.getStatus(),e);
				return null;
			} catch (ServerException e) {
				this.getResponse().setStatus(e.getStatus(),e);
				return null;
			}
		} else {
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Requested type " + mt + " is not supported");
			return null;
		}
	}
}
