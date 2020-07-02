
//Author: James Dickson <james@radiologics.com>
package org.nrg.xnat.services.triage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.helpers.file.StoredFile;
import org.nrg.xnat.helpers.resource.XnatResourceInfo;
import org.nrg.xnat.helpers.resource.direct.DirectResourceModifierBuilder;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierA;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierBuilderI;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.DataURIA;
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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

/**
 * @author james
 *
 */


@Service
public class TriageFileMover implements Mover{
	private final static Logger logger = LoggerFactory.getLogger(TriageFileMover.class);

	final String MANIFEST = ".manifest";
	
	public TriageFileMover(){ }
	
	@Override
	public File getSource(UserI user, DataURIA src){
		File srcF;
		if(src.getProps().containsKey(UriParserUtils._REMAINDER)){
			srcF=TriageUtils.getTriageFile(File.separator+"projects"+File.separator+(String)src.getProps().get(URIManager.PROJECT_ID)+File.separator+"resources"+File.separator+(String)src.getProps().get(URIManager.XNAME),File.separator+(String)src.getProps().get(UriParserUtils._REMAINDER));
		}else{
			srcF=TriageUtils.getTriageFile(File.separator+"projects"+File.separator+(String)src.getProps().get(URIManager.PROJECT_ID)+File.separator+"resources"+File.separator+(String)src.getProps().get(URIManager.XNAME));
		}
		return srcF;
	}

	@Override
	public List<String> move(UserI user,Integer eventId,Boolean overwrite,ListMultimap<String,Object> params,DataURIA src,ResourceURII dest,EventMetaI ci) throws Exception{
		List<String> duplicates=new ArrayList<String>();
		File srcF=this.getSource(user,src);
		final String label = dest.getResourceLabel();
		
		String filepath=dest.getResourceFilePath();
		if(filepath!=null && filepath.equals("/")){
			filepath=null;
		}
		
		final String type=(String)dest.getProps().get(URIManager.TYPE);
					
		if(srcF.isDirectory()){
			Collection <File> files=org.apache.commons.io.FileUtils.listFiles(srcF,null,true);
			List<StoredFile> storedFiles=new ArrayList<StoredFile>();
			for (File file : files) {
				StoredFile storedFile=new StoredFile(file, overwrite);
				if(!MANIFEST.equals(file.getName())){
					storedFiles.add(storedFile);
				}
			}
			
			duplicates=this.buildResourceModifier(user,dest,overwrite,type,ci).addFile(
					(List<? extends FileWriterWrapperI>)storedFiles,
					label,
					type, 
					filepath, 
					this.buildResourceInfo(user,params,ci),
					overwrite);
			if(duplicates.size() <=0){
				cleanup(srcF);
			}
		}else{
			duplicates= this.buildResourceModifier(user,dest,overwrite,type,ci).addFile(
				(List<? extends FileWriterWrapperI>)Lists.newArrayList(new StoredFile(srcF,overwrite)),
				label,
				type, 
				filepath, 
				this.buildResourceInfo(user,params,ci),
				overwrite);
		}
		XDAT.triggerXftItemEvent(dest.getProject(), XftItemEventI.UPDATE);
		return duplicates;
	}
	@Override
	public void cleanup(File srcF) throws FileNotFoundException, IOException{
		//cleanup the triage resource.
		if(srcF.getName().endsWith("files")){
			FileUtils.MoveToCache(srcF.getParentFile());

		}else{
			FileUtils.MoveToCache(srcF);
		}
	}

    /* (non-Javadoc)
	 * @see org.nrg.xnat.helpers.move.Mover#buildResourceInfo(org.nrg.xft.event.EventMetaI)
	 */
    @Override
	public XnatResourceInfo buildResourceInfo(UserI user,ListMultimap<String,Object> params,EventMetaI ci) throws Exception{	       		
		final String description;
	    if(!CollectionUtils.isEmpty(params.get("description"))){
	    	description=(String)(params.get("description").get(0));
	    }else{
	    	description=null;
	    }
	    
	    final String format;
	    if(!CollectionUtils.isEmpty(params.get("format"))){
	    	format=(String)(params.get("format").get(0));
	    }else{
	    	format=null;
	    }
	    
	    final String content;
	    if(!CollectionUtils.isEmpty(params.get("content"))){
	    	content=(String)(params.get("content").get(0));
	    }else{
	    	content=null;
	    }
	    
	    String[] tags;
	    if(!CollectionUtils.isEmpty(params.get("tags"))){
	    	tags = (String[])params.get("tags").toArray();
	    }else{
	    	tags=null;
	    }
        
	    Date now=EventUtils.getEventDate(ci, false);
		return XnatResourceInfo.buildResourceInfo(description, format, content, tags,user,now,now,EventUtils.getEventId(ci));
	}
    @Override
	public ResourceModifierA buildResourceModifier(UserI user,final ResourceURII arcURI,final boolean overwrite, final String type, final EventMetaI ci) throws ActionException{
		XnatImagesessiondata assessed=null;
			
		
		
		if(arcURI instanceof AssessedURII)assessed=((AssessedURII)arcURI).getSession();
			
		//this should allow dependency injection - TO
		
		try {
			if(!arcURI.getSecurityItem().canEdit(user)){
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
	