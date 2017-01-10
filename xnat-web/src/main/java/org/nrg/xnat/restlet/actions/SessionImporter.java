/*
 * web: org.nrg.xnat.restlet.actions.SessionImporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.status.ListenerUtils;
import org.nrg.status.StatusProducer;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.FinishImageUpload;
import org.nrg.xnat.helpers.PrearcImporterHelper;
import org.nrg.xnat.helpers.merge.SiteWideAnonymizer;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionException;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.restlet.actions.PrearcImporterA.PrearcSession;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.nrg.xnat.turbine.utils.XNATSessionPopulater;
import org.restlet.data.Status;
import org.xml.sax.SAXException;

@ImporterHandler(handler = ImporterHandlerA.SESSION_IMPORTER)
public class SessionImporter extends ImporterHandlerA implements Callable<List<String>> {

	static Logger logger = Logger.getLogger(SessionImporter.class);

	public static final String RESPONSE_URL = "URL";
	
	private final Boolean overrideExceptions;
	
	private final Boolean allowSessionMerge;
	
	private final FileWriterWrapperI fw;
	
	private final Object uID;
	
	private final UserI user;
	
	final Map<String,Object> params;
	
	
	/**
	 * 
	 * @param listenerControl
	 * @param u
     * @param fw
     * @param params
	 */
	public SessionImporter(final Object listenerControl, final UserI u, final FileWriterWrapperI fw, final Map<String,Object> params){
		super(listenerControl, u, fw, params);
		this.uID=listenerControl;
		this.user=u;
		this.fw=fw;
		this.params=params;
		
		String overwriteV=(String)params.remove("overwrite");
		
		if(overwriteV==null){
			this.overrideExceptions=false;
			this.allowSessionMerge=false;
		}else{
			if(overwriteV.equalsIgnoreCase(PrearcUtils.APPEND)){
				this.overrideExceptions=false;
				this.allowSessionMerge=true;
			}else if(overwriteV.equalsIgnoreCase(PrearcUtils.DELETE)){//leaving this for backwards compatibility... deprecated by 'override' setting
				this.overrideExceptions=true;
				this.allowSessionMerge=true;
			}else if(overwriteV.equalsIgnoreCase("override")){
				this.overrideExceptions=true;
				this.allowSessionMerge=true;
			} else{
				this.overrideExceptions=false;
				this.allowSessionMerge=true;
			}
		}
	}
		
	public static List<PrearcSession> importToPrearc(StatusProducer parent,String format,Object listener,UserI user,FileWriterWrapperI fw,Map<String,Object> params,boolean allowSessionMerge, boolean overwriteFiles) throws ActionException{
		//write file
		try {
			final PrearcImporterA destination = PrearcImporterA.buildImporter(format, listener, user, fw, params, allowSessionMerge, overwriteFiles);
			final PrearcImporterA listeners = ListenerUtils.addListeners(parent, destination);
			return listeners.call();
		} catch (SecurityException e) {
			throw new ServerException(e.getMessage(),e);
		} catch (IllegalArgumentException e) {
			throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,e.getMessage(),e);
		} catch (NoSuchMethodException e) {
			throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,e.getMessage(),e);
		} catch (InstantiationException e) {
			throw new ServerException(e.getMessage(),e);
		} catch (IllegalAccessException e) {
			throw new ServerException(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			throw new ServerException(e.getMessage(),e);
		} catch (PrearcImporterA.UnknownPrearcImporterException e) {
			throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND,e.getMessage(),e);
		}
	}

	public static XnatImagesessiondata getExperimentByIdorLabel(final String project, final String expt_id, final UserI user){
		XnatImagesessiondata expt=null;
		if(!StringUtils.isEmpty(project)){
			expt=(XnatImagesessiondata)XnatExperimentdata.GetExptByProjectIdentifier(project, expt_id, user, false);
		}
			
		if(expt==null){
			expt=(XnatImagesessiondata)XnatExperimentdata.getXnatExperimentdatasById(expt_id, user, false);
		}
		return expt;
	}
			
	@SuppressWarnings("serial")
	public List<String> call() throws ClientException,ServerException{
			try {
			String dest =(String)params.get(RequestUtil.DEST);

			XnatImagesessiondata expt=null;
			
			final URIManager.DataURIA destination=(!StringUtils.isEmpty(dest))?UriParserUtils.parseURI(dest):null;
			
			String project=null;
			
			Map<String,Object> prearc_parameters=new HashMap<String,Object>(params);
			
			
			//check for existing session by URI
			if(destination!=null){
				if(destination instanceof URIManager.PrearchiveURI){
					prearc_parameters.putAll(destination.getProps());
					String timezone=(String)params.get("TIMEZONE");
					if(!StringUtils.isEmpty(timezone)){
						prearc_parameters.put("TIMEZONE", timezone);
					}
					String source=(String)params.get("SOURCE");
					if(!StringUtils.isEmpty(source)){
						prearc_parameters.put("SOURCE", source);
					}
				}else{
					project=PrearcImporterHelper.identifyProject(destination.getProps());
					if(!StringUtils.isEmpty(project)){
						prearc_parameters.put("project", project);
					}
			
					if(destination.getProps().containsKey(URIManager.SUBJECT_ID)){
						prearc_parameters.put("subject_ID", destination.getProps().get(URIManager.SUBJECT_ID));
					}

					String timezone=(String)params.get("TIMEZONE");
					if(!StringUtils.isEmpty(timezone)){
						prearc_parameters.put("TIMEZONE", timezone);
					}

					String source=(String)params.get("SOURCE");
					if(!StringUtils.isEmpty(source)){
						prearc_parameters.put("SOURCE", source);
					}

					String expt_id=(String)destination.getProps().get(URIManager.EXPT_ID);
					if(!StringUtils.isEmpty(expt_id)){
						expt=getExperimentByIdorLabel(project, expt_id,user);
					}					
			
					if(expt==null){
						if(!StringUtils.isEmpty(expt_id)){
							prearc_parameters.put("label", expt_id);
						}
					}
				}
			}
			
			if(expt==null){
				if(StringUtils.isEmpty(project)){
					project=PrearcImporterHelper.identifyProject(prearc_parameters);
				}
				
				//check for existing experiment by params
				if(prearc_parameters.containsKey(URIManager.SUBJECT_ID)){
					prearc_parameters.put("xnat:subjectAssessorData/subject_ID", prearc_parameters.get(URIManager.SUBJECT_ID));
				}
									
				String expt_id=(String)prearc_parameters.get(URIManager.EXPT_ID);
				String expt_label=(String)prearc_parameters.get(URIManager.EXPT_LABEL);
				if(!StringUtils.isEmpty(expt_id)){
					expt=getExperimentByIdorLabel(project, expt_id,user);
				}
				
				if(expt==null && !StringUtils.isEmpty(expt_label)){
					expt=getExperimentByIdorLabel(project, expt_label,user);
				}
					
				if(expt==null){
					if(!StringUtils.isEmpty(expt_label)){
						prearc_parameters.put("xnat:experimentData/label", expt_label);
					}else if(!StringUtils.isEmpty(expt_id)){
						prearc_parameters.put("xnat:experimentData/label", expt_id);
					}
				}
			}
			
			//set properties to match existing session
			if(expt!=null){
				prearc_parameters.put("xnat:experimentData/project", expt.getProject());
				if(!prearc_parameters.containsKey("xnat:subjectAssessorData/subject_ID")){
				prearc_parameters.put("xnat:subjectAssessorData/subject_ID", expt.getSubjectId());
				}
				prearc_parameters.put("xnat:experimentData/label", expt.getLabel());
			}
			
			//import to prearchive, code allows for merging new files into a pre-existing session directory
			final List<PrearcSession> sessions=importToPrearc(this,(String)params.remove(PrearcImporterA.PREARC_IMPORTER_ATTR),uID,user,fw,prearc_parameters,allowSessionMerge,overrideExceptions);
			
			if(sessions.size()==0){
				failed("Upload did not include parseable files for session generation.");
				throw new ClientException("Upload did not include parseable files for session generation.");
			}
			
			//if prearc=destination, then return
			if(destination!=null && destination instanceof URIManager.PrearchiveURI){
				this.completed("Successfully uploaded " + sessions.size() +" sessions to the prearchive.");
				resetStatus(sessions);
				return returnURLs(sessions);
			}

			
			//if unknown destination, only one session supported
			if(sessions.size()>1){
				resetStatus(sessions);
				failed("Upload included files for multiple imaging sessions.");
				throw new ClientException("Upload included files for multiple imaging sessions.");
			}
			
			final PrearcSession session = sessions.get(0);
			session.getAdditionalValues().putAll(params);
				
			try {
				final FinishImageUpload finisher=ListenerUtils.addListeners(this, new FinishImageUpload(this.uID, user, session,destination, overrideExceptions,allowSessionMerge,true));
				XnatImagesessiondata s = new XNATSessionPopulater(user, session.getSessionDir(), session.getProject(), false).populate();
				SiteWideAnonymizer site_wide = new SiteWideAnonymizer(s, true);
				site_wide.call();
				if(finisher.isAutoArchive()){
					return new ArrayList<String>(){{add(finisher.call());}};
				}else{
					this.completed("Successfully uploaded " + sessions.size() +" sessions to the prearchive.");
					resetStatus(sessions);
					return returnURLs(sessions);
				}
			} catch (Exception e) {
				resetStatus(sessions);
                if(e instanceof ClientException && Status.CLIENT_ERROR_CONFLICT.equals(((ClientException)e).getStatus())){
                    //if this failed due to a conflict
                    PrearcDatabase.setStatus(session.getSessionDir().getName(), session.getTimestamp(), session.getProject(), PrearcUtils.PrearcStatus.CONFLICT);
                }else{
                    PrearcDatabase.setStatus(session.getSessionDir().getName(), session.getTimestamp(), session.getProject() , PrearcUtils.PrearcStatus.ERROR);
                }
				throw e;
			}
			
		} catch (ClientException e) {
			this.failed(e.getMessage());
			throw e;
		} catch (ServerException e) {
			this.failed(e.getMessage());
			throw e;
		} catch (IOException e) {
			logger.error("",e);
			this.failed(e.getMessage());
			throw new ServerException(e.getMessage(),e);
		} catch (SAXException e) {
			logger.error("",e);
			this.failed(e.getMessage());
			throw new ClientException(e.getMessage(),e);
		} catch (Throwable e) {
			logger.error("",e);
			throw new ServerException(e.getMessage(),new Exception());
		}
	}
	
	public List<String> returnURLs(final List<PrearcSession> sessions)throws ActionException{
		List<String> _return= new ArrayList<String>();
		for(final PrearcSession ps: sessions){
			_return.add(ps.getUrl());
		}
		return _return;
	}
	
	public void resetStatus(final List<PrearcSession> sessions)throws ActionException{
		for(final PrearcSession ps:sessions){

			try {
				Map<String,Object> session = PrearcUtils.parseURI(ps.getUrl());
				try {
					PrearcUtils.addSession(user, (String) session.get(URIManager.PROJECT_ID), (String) session.get(PrearcUtils.PREARC_TIMESTAMP), (String) session.get(PrearcUtils.PREARC_SESSION_FOLDER),true);
				} catch (SessionException e) {
					PrearcUtils.resetStatus(user, (String) session.get(URIManager.PROJECT_ID), (String) session.get(PrearcUtils.PREARC_TIMESTAMP), (String) session.get(PrearcUtils.PREARC_SESSION_FOLDER),true);
				}
			} catch (InvalidPermissionException e) {
				logger.error("",e);
				throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN,e);
			} catch (Exception e) {
				logger.error("",e);
				throw new ServerException(e);
			}
		}
	}
	
}
