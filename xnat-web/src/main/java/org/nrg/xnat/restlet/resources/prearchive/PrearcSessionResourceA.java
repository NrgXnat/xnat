/*
 * org.nrg.xnat.restlet.resources.prearchive.PrearcSessionResourceA
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
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
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Variant;

import java.io.File;

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
			sessionDIR = PrearcUtils.getPrearcSessionDir(user, project, timestamp, session,false);
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

}