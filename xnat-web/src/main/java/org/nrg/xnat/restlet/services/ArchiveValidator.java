/*
 * org.nrg.xnat.restlet.services.ArchiveValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 9/9/13 1:04 PM
 */
package org.nrg.xnat.restlet.services;

import com.google.common.collect.Lists;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xnat.archive.PrearcSessionValidator;
import org.nrg.xnat.archive.PrearcSessionValidator.Notice;
import org.nrg.xnat.helpers.PrearcImporterHelper;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase.SyncFailedException;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.restlet.actions.PrearcImporterA.PrearcSession;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.restlet.services.prearchive.BatchPrearchiveActionsA;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.*;

public class ArchiveValidator extends SecureResource {
	private static final String PROJECT = "project";
	private static final String DEST = "dest";
	
	private final static Logger logger = LoggerFactory.getLogger(Archiver.class);
	
	XFTTable t;
	
	public ArchiveValidator(Context context, Request request, Response response) {
		super(context, request, response);


		t= new XFTTable();
		t.initTable(new String[]{"code","type","message"}); 
	}
	
	final Map<String,Object> additionalValues=new Hashtable<String,Object>();
	
	String project_id=null;
	String timestamp=null;
	List<String> sessionFolder=Lists.newArrayList();
	String dest=null;
	protected List<String> srcs = new ArrayList<String>();
			
	@Override
	public void handleParam(final String key,final Object value) throws ClientException {
			if(value !=null){
				if(key.equals(PROJECT)){
				additionalValues.put("project",value);
				}else if(key.equals(PrearcUtils.PREARC_TIMESTAMP)){
				timestamp=(String)value;
				}else if(key.equals(PrearcUtils.PREARC_SESSION_FOLDER)){
				sessionFolder.add((String)value);
				}else if(key.equals(DEST)){
				dest=(String)value;
				}else if(key.equals(BatchPrearchiveActionsA.SRC)){
				srcs.add((String)value);
				}else{
				additionalValues.put(key,value);
			}
		}
	}

	@Override
	public boolean allowGet() {
		return false;
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public void handlePost() {		
		
		//build fileWriters
		try {					
			loadQueryVariables();
			loadBodyVariables();
						
			final List<PrearcSession> sessions=new ArrayList<PrearcSession>();
						
			project_id=PrearcImporterHelper.identifyProject(additionalValues);
			
			if((project_id==null || timestamp==null || sessionFolder==null) && (srcs==null)){
				this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unknown prearchive session.");
				return;
			}else if(srcs!=null){
				for(final String src: srcs){
					URIManager.DataURIA data;
					try {
						data = UriParserUtils.parseURI(src);
					} catch (MalformedURLException e) {
						this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
						return;
					}
					if(data instanceof URIManager.ArchiveURI){
						this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid src URI (" + src +")");
						return;
					}
					
					try {
						sessions.add(new PrearcSession((URIManager.PrearchiveURI)data,additionalValues,user));
					} catch (InvalidPermissionException e) {
						throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, data.getUri());
					} catch (Exception e) {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, data.getUri()+" invalid.");
					}
				}
			}else if(dest!=null){
				URIManager.DataURIA data;
				try {
					data = UriParserUtils.parseURI(dest);
				} catch (MalformedURLException e) {
					this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
					return;
				}
				if(data instanceof URIManager.PrearchiveURI){
					this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid dest URI (" + dest +")");
					return;
				}
				additionalValues.putAll(data.getProps());
			}else{
				for(final String s:sessionFolder){
					try {
						sessions.add(new PrearcSession(project_id, timestamp, s, additionalValues,user));
					} catch (InvalidPermissionException e) {
						throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, String.format("/prearchive/projects/%s/%s/%s not found.",project_id, timestamp, s));
					} catch (Exception e) {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, String.format("/prearchive/projects/%s/%s/%s invalid.",project_id, timestamp, s));
					}
				}
			}
			
			//validate specified folders
			for(final PrearcSession map: sessions){
				if(map.getProject()==null){
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Cannot archive sessions from the Unassigned folder.");
				}
				
				if(!map.getSessionDir().exists()){
					throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, map.getUrl() + " not found.");
				}
			}
							
			if(sessions.size()==1){
				
				final PrearcSession session=sessions.get(0);
				
				if (!PrearcUtils.canModify(user, session.getProject())) {
					this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Invalid permissions for new project.");
					return;
				}
				
				PrearcSessionValidator validator=new PrearcSessionValidator(session, user, additionalValues);
				List<PrearcSessionValidator.Notice> validation=validator.validate();
				
				Collections.sort(validation,new Comparator<Notice>() {
					@Override
					public int compare(Notice o1, Notice o2) {
						return o1.getCode()-o2.getCode();
					}
				});
				
				for(PrearcSessionValidator.Notice notice:validation){
					t.rows().add(new Object[]{notice.getCode(),notice.getType(),notice.getMessage()});
				}

				getResponse().setEntity(representTable(t,overrideVariant(getPreferredVariant()),new Hashtable<String, Object>()));
				
				return;
				
			}else{				
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Cannot validate multiple sessions in one request.");
			}
		} catch (ActionException e) {
			logger.error("",e);
			this.getResponse().setStatus(e.getStatus(), e.getMessage());
		} catch (SyncFailedException e) {
			if(e.cause!=null && e.cause instanceof ActionException){
				logger.error("",e.cause);
				this.getResponse().setStatus(((ActionException)e.cause).getStatus(), e.cause.getMessage());
				return;
			}else{
				logger.error("",e);
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e);
				return;
			}
		} catch (ResourceException e) {
			logger.error("",e);
			this.getResponse().setStatus(e.getStatus(), e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("",e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
		} catch (Exception e) {
			logger.error("",e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e);
			return;
		}
	}
}
