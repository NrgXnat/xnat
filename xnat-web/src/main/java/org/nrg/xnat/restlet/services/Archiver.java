/*
 * web: org.nrg.xnat.restlet.services.Archiver
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.services;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.status.StatusListenerI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.FinishImageUpload;
import org.nrg.xnat.archive.PrearcSessionArchiver;
import org.nrg.xnat.helpers.PrearcImporterHelper;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase.SyncFailedException;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.nrg.xnat.helpers.prearchive.SessionDataTriple;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.restlet.actions.PrearcImporterA.PrearcSession;
import org.nrg.xnat.restlet.services.prearchive.BatchPrearchiveActionsA;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

public class Archiver extends BatchPrearchiveActionsA  {
	private static final String REDIRECT2 = "redirect";
	private static final String FOLDER = "folder";
	private static final String OVERWRITE = "overwrite";
	private static final String OVERWRITE_FILES = "overwrite_files";
	private static final String PROJECT = "project";
	private static final String CRLF = "\r\n";
	private static final String DEST = "dest";
	
	private final static Logger logger = LoggerFactory.getLogger(Archiver.class);
	
	public Archiver(Context context, Request request, Response response) {
		super(context, request, response);
				
	}
	
	final Map<String,Object> additionalValues= new HashMap<String, Object>();
	
	String project_id=null;
	String overwriteV=null;
	String overwriteFILES=null;
	String timestamp=null;
	List<String> sessionFolder=Lists.newArrayList();
	String dest=null;
	String redirect=null;
			
	@Override
	public void handleParam(final String key,final Object value) throws ClientException {
			//if(value !=null){
				if(key.equals(PROJECT)){
				additionalValues.put("project",value);
				}else if(key.equals(PrearcUtils.PREARC_TIMESTAMP)){
				timestamp=(String)value;
				}else if(key.equals(PrearcUtils.PREARC_SESSION_FOLDER)){
				sessionFolder.add((String)value);
				}else if(key.equals(OVERWRITE_FILES)){
				overwriteFILES=(String)value;
				}else if(key.equals(OVERWRITE)){
				overwriteV=(String)value;
				}else if(key.equals(DEST)){
				dest=(String)value;
				}else if(key.equals(SRC)){
				srcs.add((String)value);
				}else if(key.equals(REDIRECT2)){
				redirect=(String)value;
				}else{
				additionalValues.put(key,value);
			//}
		}
	}

	@Override
	public void handlePost() {		
		//build fileWriters
		try {					
			loadBodyVariables();
			loadQueryVariables();
			
			
			boolean allowDataDeletion=false;
			boolean overwrite=false;
			
			if(overwriteV==null){
				allowDataDeletion=false;
				overwrite=false;
			}else{
				if(overwriteV.equalsIgnoreCase(PrearcUtils.APPEND)){
					allowDataDeletion=false;
					overwrite=true;
				}else if(overwriteV.equalsIgnoreCase(PrearcUtils.DELETE)){
					allowDataDeletion=true;
					overwrite=true;
				} else{
					allowDataDeletion=false;
					overwrite=false;
				}
			}
			
			final boolean overwrite_files;
			
			if(overwriteFILES!=null && overwriteFILES.toString().equalsIgnoreCase(RequestUtil.TRUE)){
				overwrite_files=true;
			}else{
				overwrite_files=false;
			}
			
			final List<PrearcSession> sessions=new ArrayList<PrearcSession>();
						
			project_id=PrearcImporterHelper.identifyProject(additionalValues);

			final UserI user = getUser();
			if((project_id == null || timestamp == null || sessionFolder == null) && (srcs == null)){
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
						sessions.add(new PrearcSession((URIManager.PrearchiveURI)data, additionalValues, user));
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
						sessions.add(new PrearcSession(project_id, timestamp, s, additionalValues, user));
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
				
			Set<StatusListenerI> listeners=(Set<StatusListenerI>)Collections.EMPTY_SET;
			
			if(sessions.size()==1){
				String _return;
				
				final PrearcSession session=sessions.get(0);
				
				if (!PrearcUtils.canModify(user, session.getProject())) {
					this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Invalid permissions for new project.");
					return;
				}
				
				if(PrearcDatabase.setStatus(session.getFolderName(), session.getTimestamp(), session.getProject(), PrearcStatus.ARCHIVING)){
					FinishImageUpload.setArchiveReason(session, false);
					_return = "/data" +PrearcDatabase.archive(session, allowDataDeletion, overwrite, overwrite_files, user, listeners);
				}else{
					this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Operation already in progress on this prearchive entry.");
					return;
				}
								
				if(!StringUtils.isEmpty(redirect) && redirect.equalsIgnoreCase("true")){
					getResponse().redirectSeeOther(getContextPath()+_return);
				}else{
					getResponse().setEntity(_return+CRLF, MediaType.TEXT_URI_LIST);
				}
				return;
				
			}else{				
				Map<SessionDataTriple,Boolean> m;
				
				if (!PrearcUtils.canModify(user, sessions.get(0).getProject())) {
					this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Invalid permissions for new project.");
					return;
				}
				
				for(PrearcSession ps:sessions){
					if(!ps.getAdditionalValues().containsKey(EventUtils.EVENT_REASON))
						ps.getAdditionalValues().put(EventUtils.EVENT_REASON, "Batch archive");
				}
				
				m=PrearcDatabase.archive(sessions, allowDataDeletion, overwrite, overwrite_files, user, listeners);

								
				getResponse().setEntity(updatedStatusRepresentation(m.keySet(),overrideVariant(getPreferredVariant())));
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
	
	public static File getSrcDIR(Map<String,Object> params){
		return (File)params.get(FOLDER);
	}
		
	public static PrearcSessionArchiver buildArchiver(final PrearcSession session, final Boolean overrideExceptions,final Boolean allowSessionMerge,final Boolean overwriteFiles,final UserI user, final boolean waitFor) throws IOException, SAXException {
		final PrearcSessionArchiver archiver;

		archiver = new PrearcSessionArchiver(session, user, session.getAdditionalValues(), overrideExceptions,allowSessionMerge, waitFor,overwriteFiles);
			
		return archiver;
	}
}
