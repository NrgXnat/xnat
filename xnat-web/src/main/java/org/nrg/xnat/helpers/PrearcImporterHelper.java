/*
 * web: org.nrg.xnat.helpers.PrearcImporterHelper
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.status.ListenerUtils;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.helpers.merge.MergePrearchiveSessions;
import org.nrg.xnat.helpers.merge.MergeSessionsA.SaveHandlerI;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcTableBuilder;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.nrg.xnat.restlet.actions.PrearcImporterA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.turbine.utils.ImageUploadHelper;
import org.restlet.data.Status;

public class PrearcImporterHelper extends PrearcImporterA{
    private static final String SESSION = "session";
	private static final String SUBJECT = "subject";
	static org.apache.log4j.Logger logger = Logger.getLogger(PrearcImporterHelper.class);
	
	
	private final FileWriterWrapperI fi;
	private final UserI user;
	
	private final Map<String,Object> params;
	private final Object uID;
	
	private final boolean allowSessionMerge,overwriteFiles;
	
	/**
	 * Helper class to extract a passed zip into the prearchive.
	 * @param uID2
	 * @param u
	 * @param fi
	 * @param project_id
	 * @param additionalValues
	 */
	public PrearcImporterHelper(final Object uID2, final UserI u, final FileWriterWrapperI fi, Map<String,Object> params,boolean allowSessionMerge,boolean overwriteFiles){
    	super((uID2==null)?u:uID2,u,fi,params,allowSessionMerge,overwriteFiles);
    	this.user=u;
    	this.uID=(uID2==null)?u:uID2;
		this.fi=fi;
		this.allowSessionMerge=allowSessionMerge;
		this.overwriteFiles=overwriteFiles;
		this.params=params;
	}
	
	public static String identifyProject(Map<String,Object> params){
		if(params.containsKey("PROJECT_ID")){
			return (String)params.get("PROJECT_ID");
		}
		if(params.containsKey("project")){
			return (String)params.get("project");
		}
		return null;
	}
			
	public List<PrearcSession> call() throws ActionException{
		final String project=identifyProject(params);
		final String old_session_folder=(String)params.get(PrearcUtils.PREARC_SESSION_FOLDER);
		String old_timestamp=(String)params.get(PrearcUtils.PREARC_TIMESTAMP);
			
		//TODO this should use PrearcUtils.makeTimes...
		final String new_timestamp = PrearcUtils.makeTimestamp();
		
		final File cacheDIR=write_to_cache(user,fi,new_timestamp);
		
		//IF timestamp,session is specified then push to a temporary space and merge
		//ELSE import directly to project.
		SessionData sd =null;
		boolean destination_specified=false;
		if(StringUtils.isNotEmpty(old_timestamp)&& StringUtils.isNotEmpty(old_session_folder)){
			destination_specified=true;
			if(StringUtils.isEmpty(project)){
				this.failed("User must specify project portion of prearchive path, if timestamp/session portion is specified.");
				throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,"User must specify project portion of prearchive path, if timestamp/session portion is specified.",new IllegalArgumentException());
			}
			
			try {
				sd=PrearcDatabase.getSession(old_session_folder, old_timestamp, project);
				if(sd!=null){
					if(!PrearcDatabase.setStatus(old_session_folder, old_timestamp, project, PrearcStatus._RECEIVING)){
						this.failed("Prearc destination is locked.");
						throw new ClientException(Status.CLIENT_ERROR_CONFLICT,"Prearc destination is locked.",new IllegalArgumentException());
					}
				}
			} catch (Exception e) {
				logger.error("",e);
			}
		}else if(StringUtils.isNotEmpty(old_session_folder)){
			if(StringUtils.isEmpty(old_timestamp)){
				this.failed("User must specify timestamp portion of prearchive path, if session portion is specified.");
				throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,"User must specify timestamp portion of prearchive path, if session portion is specified.",new IllegalArgumentException());
			}
		}
		
		List<File> files;
		try {
			files = null;
			
			final Map<String,Object> additionalValues=XMLPathShortcuts.identifyUsableFields(params,XMLPathShortcuts.EXPERIMENT_DATA,false);
			if(params.containsKey(SUBJECT)){
				additionalValues.put("xnat:subjectAssessorData/subject_ID".intern(), params.remove(SUBJECT));
			}

			if(params.containsKey(SESSION)){
				additionalValues.put("xnat:experimentData/label".intern(), params.remove(SESSION));
			}
			if(params.containsKey("TIMEZONE")){
				additionalValues.put("TIMEZONE", params.get("TIMEZONE"));
			}
			if(params.containsKey("SOURCE")){
				additionalValues.put("SOURCE", params.get("SOURCE"));
			}
			if(StringUtils.isEmpty(old_timestamp))old_timestamp=new_timestamp;
								
			if(destination_specified || project==null){
				File projectPrearc;
				if(project==null){
					projectPrearc=new File(ArcSpecManager.GetInstance().getGlobalPrearchivePath(),PrearcUtils.TEMP_UNPACK);
				}else{
					projectPrearc=new File(ArcSpecManager.GetInstance().getPrearchivePathForProject(project));
				}

				//CAn this return the project?
				List<File> tempFiles=reorganize(cacheDIR, new File(projectPrearc,new_timestamp),null, additionalValues);
				
				//should 2-srcs to project=null also be merged?
				//Merge_to_destination will write it to separate folders if old_session_folder is null
				files=new ArrayList<File>();
				for(final File f:tempFiles){
					File builtF=merge_to_destination(project,old_timestamp,old_session_folder,f);
					if(!files.contains(builtF)){
						files.add(builtF);
					}
				}
			}else{
				files=reorganize(cacheDIR, new File(ArcSpecManager.GetInstance().getPrearchivePathForProject(project),old_timestamp),project, additionalValues);
			}
		} catch (ActionException e1) {
			if(sd!=null){
				try {
					PrearcUtils.resetStatus(user, project, old_timestamp, old_session_folder,true);
				} catch (Exception e) {
					logger.error("",e);
				}
			}
			throw e1;	
		}
					
		final List<PrearcSession> sessions= new ArrayList<PrearcSession>();
		
		for(final File f:files){
		    try {
				sessions.add(new PrearcSession(f));
			} catch (Exception e) {
				throw new ServerException(e.getMessage(),e);
			}
		}
		
		return sessions;
	}
	
	private File write_to_cache(final UserI user, final FileWriterWrapperI fi,final String timestamp) throws ServerException{
			final String filename = fi.getName();
		this.processing("Importing file (" + filename + ")");
			
		//BUILD CACHE PATH        
		final File cacheDIR = new File(new File(Users.getUserCacheFile(user, "uploads"),user.getID().toString()),timestamp);
		if (!cacheDIR.exists()){
			this.processing("mkdir " + cacheDIR.getAbsolutePath());
			cacheDIR.mkdirs();
		}
			
		final File uploaded = new File(cacheDIR,cleanFileName(filename)) ;
			                
		this.processing("Uploading to "+uploaded.getAbsolutePath() + " ... ");
			
		try {
			fi.write(uploaded);
		} catch (Exception e1) {
			throw new ServerException(e1.getMessage(),e1);
		}

		this.processing("file uploaded");
		
		return cacheDIR;
	}
	
	private File merge_to_destination(String project, final String timestamp,String sessionFolder, File srcDIR) throws ClientException,ServerException{
		
		if(project==null){
			//determine correct project
			project=identifyProject(srcDIR,user);
		}
		String prearc_path;
		if(project!=null)
			prearc_path=ArcSpecManager.GetInstance().getPrearchivePathForProject(project);
		else{
			prearc_path=ArcSpecManager.GetInstance().getGlobalPrearchivePath();
		}
		
		if(sessionFolder==null){
			sessionFolder=srcDIR.getName();
		}
		
		final File destDIR=new File(new File(prearc_path,timestamp),sessionFolder);
		
		final File srcXML=new File(srcDIR.getAbsolutePath()+".xml");
		final File destXML=new File(destDIR.getAbsolutePath()+".xml");

		final XnatImagesessiondataBean src;
		try {
			src=PrearcTableBuilder.parseSession(srcXML);
			src.setProject(project);
		} catch (Exception e) {
			failed("Unable to parse meta-data for uploaded data.");
			throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,e.getMessage(),e);
		}

		XnatImagesessiondataBean dest=null;
		if(destDIR.exists()){
			try {
				dest=PrearcTableBuilder.parseSession(destXML);
			} catch (Exception e) {
				failed("Unable to parse meta-data for existing data.");
				throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,e.getMessage(),e);
			}
		}
		
		SaveHandlerI<XnatImagesessiondataBean> saveImpl=new SaveHandlerI<XnatImagesessiondataBean>() {
			public void save(XnatImagesessiondataBean merged) throws Exception {
				FileWriter fw = new FileWriter(destXML);
				merged.toXML(fw);
				fw.close();
			}
		};
		
		//pass in populated beans and root paths
		ListenerUtils.addListeners(this,new MergePrearchiveSessions(uID,srcDIR,src,src.getPrearchivepath(),destDIR,dest,destDIR.getAbsolutePath(),allowSessionMerge,overwriteFiles,saveImpl,user))
			.call();

		org.nrg.xft.utils.FileUtils.DeleteFile(srcXML);
		org.nrg.xft.utils.FileUtils.deleteDirQuietly(srcDIR);
		
		if(!FileUtils.HasFiles(srcDIR.getParentFile())){
			org.nrg.xft.utils.FileUtils.deleteDirQuietly(srcDIR.getParentFile());
		}
		
		return destDIR;
		
	}
	
	private static String identifyProject(File f, final UserI user) throws ClientException,ServerException{
		try {
			if(!f.getName().endsWith(".xml"))f=new File(f.getParentFile(),f.getName()+".xml");
			final XnatImagesessiondataI session=PrearcTableBuilder.parseSession(f);
			final String project =session.getProject();
			
			if(!StringUtils.isEmpty(project)){
				XnatProjectdata proj=XnatProjectdata.getProjectByIDorAlias(project, user, false);
				if(proj!=null){
					return proj.getId();
				}
			}
		} catch (Exception e) {
			throw new ServerException(e.getMessage(),e);
		}
		
		return null;
	}
	
	public List<File> reorganize(final File cacheDIR, final File prearcDIR, final String project,Map<String,Object> additionalValues) throws ClientException,ServerException{
			if (!prearcDIR.exists()){
			this.processing("mkdir " + prearcDIR.getAbsolutePath());
			    prearcDIR.mkdirs();
			}
			
		this.processing("Importing to " + prearcDIR.getAbsolutePath());
	    prearcDIR.mkdirs();
		
		return ListenerUtils.addListeners(this,new ImageUploadHelper(uID,this.user,project,cacheDIR, prearcDIR,additionalValues))
			.call();
			}
			
	
	public static String cleanFileName(String filename){
		int index = filename.lastIndexOf('\\');
        if (index< filename.lastIndexOf('/'))index = filename.lastIndexOf('/');
        if(index>0)filename = filename.substring(index+1);
        return filename;
	}
}
