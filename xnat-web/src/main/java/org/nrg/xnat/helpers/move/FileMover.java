/*
 * web: org.nrg.xnat.helpers.move.FileMover
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.move;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.file.StoredFile;
import org.nrg.xnat.helpers.resource.XnatResourceInfo;
import org.nrg.xnat.helpers.resource.direct.DirectResourceModifierBuilder;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierA;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierBuilderI;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.helpers.uri.archive.AssessedURII;
import org.nrg.xnat.helpers.uri.archive.AssessorURII;
import org.nrg.xnat.helpers.uri.archive.ExperimentURII;
import org.nrg.xnat.helpers.uri.archive.ProjectURII;
import org.nrg.xnat.helpers.uri.archive.ReconURII;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.helpers.uri.archive.ScanURII;
import org.nrg.xnat.helpers.uri.archive.SubjectURII;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

public class FileMover {
	private final static Logger logger = LoggerFactory.getLogger(FileMover.class);
	
	final boolean overwrite;
	final UserI user;
	final ListMultimap<String,Object> params;
	
	public FileMover(Boolean overwrite, UserI user, ListMultimap<String,Object> params){
		if(overwrite==null){
			this.overwrite=false;
		}else{
			this.overwrite=overwrite;
		}
		
		this.user=user;
		this.params=params;
	}
	
	public Boolean call(org.nrg.xnat.helpers.uri.URIManager.UserCacheURI src,ResourceURII dest,EventMetaI ci) throws Exception {
		File srcF;		
		if(src.getProps().containsKey(UriParserUtils._REMAINDER)){
			srcF=org.nrg.xdat.security.helpers.Users.getUserCacheFile(user, (String)src.getProps().get(URIManager.XNAME), (String)src.getProps().get(UriParserUtils._REMAINDER));
		}else{
			srcF=org.nrg.xdat.security.helpers.Users.getUserCacheFile(user, (String)src.getProps().get(URIManager.XNAME));
		}
		
		
		
		final String label = dest.getResourceLabel();
		
		String filepath=dest.getResourceFilePath();
		if(filepath!=null && filepath.equals("/")){
			filepath=null;
		}
		
		final String type=(String)dest.getProps().get(URIManager.TYPE);
						
		this.buildResourceModifier(dest,overwrite,type,ci).addFile(
				(List<? extends FileWriterWrapperI>)Lists.newArrayList(new StoredFile(srcF,overwrite)),
				label,
				type, 
				filepath, 
				this.buildResourceInfo(ci),
				overwrite);
		
		return Boolean.TRUE;
	}
	
    public XnatResourceInfo buildResourceInfo(EventMetaI ci){        		
		final String description;
	    if(!CollectionUtils.isEmpty(params.get("description"))){
	    	description=(String)(this.params.get("description").get(0));
	    }else{
	    	description=null;
	    }
	    
	    final String format;
	    if(!CollectionUtils.isEmpty(params.get("format"))){
	    	format=(String)(this.params.get("format").get(0));
	    }else{
	    	format=null;
	    }
	    
	    final String content;
	    if(!CollectionUtils.isEmpty(params.get("content"))){
	    	content=(String)(this.params.get("content").get(0));
	    }else{
	    	content=null;
	    }
	    
	    String[] tags;
	    if(!CollectionUtils.isEmpty(params.get("tags"))){
	    	tags = (String[])this.params.get("tags").toArray();
	    }else{
	    	tags=null;
	    }
        
	    Date now=EventUtils.getEventDate(ci, false);
		return XnatResourceInfo.buildResourceInfo(description, format, content, tags,user,now,now,EventUtils.getEventId(ci));
	}

	protected ResourceModifierA buildResourceModifier(final ResourceURII arcURI,final boolean overwrite, final String type, final EventMetaI ci) throws ActionException{
		XnatImagesessiondata assessed=null;
			
		
		
		if(arcURI instanceof AssessedURII)assessed=((AssessedURII)arcURI).getSession();
			
		//this should allow dependency injection - TO
		
		try {
			if(!Permissions.canEdit(user,arcURI.getSecurityItem())){
				throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, new Exception("Unauthorized attempt to add a file to "+ arcURI.getUri()));
			}
		} catch (Exception e) {
			logger.error("",e);
			throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, e);
		}

		final ResourceModifierBuilderI builder=new DirectResourceModifierBuilder();
		
		if(arcURI instanceof ReconURII){
			//reconstruction						
			builder.setRecon(assessed,((ReconURII)arcURI).getRecon(), type);
		}else if(arcURI instanceof ScanURII){
			//scan
			builder.setScan(assessed, ((ScanURII)arcURI).getScan());
		}else if(arcURI instanceof AssessorURII){//			experiment
			builder.setAssess((XnatImagesessiondata)assessed, ((AssessorURII)arcURI).getAssessor(), type);
		}else if(arcURI instanceof ExperimentURII){
			XnatExperimentdata expt=((ExperimentURII)arcURI).getExperiment();
			builder.setExpt(expt.getPrimaryProject(false),expt);
		}else if(arcURI instanceof SubjectURII){
			XnatSubjectdata sub=((SubjectURII)arcURI).getSubject();
			builder.setSubject(sub.getPrimaryProject(false), sub);
		}else if(arcURI instanceof ProjectURII){
			builder.setProject(((ProjectURII)arcURI).getProject());
		}else{
			throw new ClientException("Unsupported resource:"+arcURI.getUri());
		}
		
		try {
			return builder.buildResourceModifier(overwrite,user,ci);
		} catch (Exception e) {
			throw new ServerException(e);
		}
	}
}
