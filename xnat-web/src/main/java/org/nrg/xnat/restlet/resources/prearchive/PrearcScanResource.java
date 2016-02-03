/*
 * org.nrg.xnat.restlet.resources.prearchive.PrearcScanResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/3/14 9:54 AM
 */

/**
 * 
 */
package org.nrg.xnat.restlet.resources.prearchive;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.bean.XnatImagescandataBean;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
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

/**
 * @author tolsen01
 *
 */
public class PrearcScanResource extends PrearcSessionResourceA {
	private static final String SCAN_ID = "SCAN_ID";

	static Logger logger = Logger.getLogger(PrearcScanResource.class);

	protected final String scan_id;
	
	public PrearcScanResource(Context context, Request request,
			Response response) {
		super(context, request, response);
		scan_id = (String)getParameter(request,SCAN_ID);
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
		File sessionDIR;
		File srcXML;
		try {
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
			
			final XnatImagesessiondataBean om=PrearcTableBuilder.parseSession(srcXML);
			
			final List<XnatImagescandataBean> scans=om.getScans_scan();
			XnatImagescandataBean scan=null;
			for(int i=0;i<scans.size();i++){
				if(StringUtils.equals(scans.get(i).getId(),scan_id)){
					scan=scans.remove(i);
					break;
				}
			}
			
			if(scan==null){
				throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND,"Unknown scan " + scan_id, new Exception());
			}else{
				File scanDir=new File(new File(sessionDIR,"SCANS"),scan_id);
				if(!scanDir.exists()){
					throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND,"Unknown scan " + scan_id, new Exception());
				}else{
					try {
						FileUtils.MoveToCache(scanDir);
					} catch (Exception e) {
						logger.error("",e);
						PrearcUtils.log(project,timestamp,session, e);
						throw new ServerException(Status.SERVER_ERROR_INTERNAL,"Failed to delete files.",e);
					}
					
					om.setScans_scan((ArrayList)scans);
					
					FileWriter fw=null;
					try {
						fw = new FileWriter(srcXML);
						om.toXML(fw);
						
						PrearcUtils.log(project,timestamp,session, new Exception("Deleted scan " + scan_id));
						this.getResponse().setStatus(Status.SUCCESS_OK);
					}catch (Exception e){
						logger.error("",e);
						PrearcUtils.log(project,timestamp,session, e);
						throw new ServerException(Status.SERVER_ERROR_INTERNAL,"Failed to update session xml.",e);
					}finally{
						try {
							if(fw!=null){
								fw.close();
							}
						} catch (IOException e) {
						}
					}
				}
			}
		} catch (ClientException e) {
			this.getResponse().setStatus(e.getStatus(),e);
		} catch (ServerException e) {
			this.getResponse().setStatus(e.getStatus(),e);
		} catch (IOException e) {
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e);
		} catch (SAXException e) {
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e);
		}
		
	}
	
	@Override
	public Representation represent(final Variant variant) throws ResourceException {
		final MediaType mt=overrideVariant(variant);
		
		if (MediaType.APPLICATION_GNU_ZIP.equals(mt) || MediaType.APPLICATION_ZIP.equals(mt)) {
			try {
				final File sessionDIR;
				final File srcXML;
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
				
				final XnatImagesessiondataBean session=PrearcTableBuilder.parseSession(srcXML);
				
				File scandir=new File(new File(sessionDIR,"SCANS"),scan_id);
				
				final ZipRepresentation zip;
				try{
					zip = new ZipRepresentation(mt, scandir.getName(),identifyCompression(null));
				} catch (ActionException e) {
					logger.error("",e);
					this.setResponseStatus(e);
					return null;
				}
				zip.addFolder(scandir.getName(), scandir);
				return zip;
			} catch (ClientException e) {
				this.getResponse().setStatus(e.getStatus(),e);
			    return null;
			} catch (ServerException e) {
				this.getResponse().setStatus(e.getStatus(),e);
			    return null;
			} catch (IOException e) {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e);
			    return null;
			} catch (SAXException e) {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e);
			    return null;
		    }
        } else {
            this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Requested type " + mt + " is not supported");
            return null;
        }
	}
}
