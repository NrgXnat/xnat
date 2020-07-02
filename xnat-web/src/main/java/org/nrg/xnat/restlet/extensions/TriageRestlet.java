
//Author: James Dickson <james@radiologics.com>
package org.nrg.xnat.restlet.extensions;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.helpers.Features;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.exception.InvalidItemException;
import org.nrg.xft.utils.zip.TarUtils;
import org.nrg.xft.utils.zip.ZipI;
import org.nrg.xft.utils.zip.ZipUtils;
import org.nrg.xnat.helpers.FileWriterWrapper;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.DataURIA;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.restlet.XnatRestlet;
import org.nrg.xnat.restlet.representations.ZipRepresentation;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.services.triage.TriageManifest;
import org.nrg.xnat.services.triage.TriageUtils;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import com.google.common.collect.Maps;


@XnatRestlet(value={
		"/services/triage/projects/{PROJECT}/resources",
		"/services/triage/projects/{PROJECT}/resources/{XNAME}",
		"/services/triage/projects/{PROJECT}/resources/{XNAME}/files",
		"/services/triage/projects/{PROJECT}/resources/{XNAME}/files/{FILE}"
		})
public class TriageRestlet extends SecureResource {
	private static final String FORMAT = "format";
	private static final String CONTENT = "content";
	private static final String _ON_SUCCESS_RETURN_JS = "_onSuccessReturnJS";
	private static final String _ON_FAILURE_RETURN_JS = "_onFailureReturnJS";
	
	private static final String _ON_SUCCESS_RETURN_HTML = "_onSuccessReturnHTML";
	private static final String _ON_FAILURE_RETURN_HTML = "_onFailureReturnHTML";

	static Logger logger = Logger.getLogger(TriageRestlet.class);

	static final String[] zipExtensions={".zip",".jar",".rar",".ear",".gar",".xar"};
	private enum CompressionMethod { ZIP, TAR, GZ, NONE }
	
	private final String MANIFEST=".manifest";
	private final String TRIAGE="/services/triage/";
	private final String ARCHIVE="/archive/";
	private final String RESOURCE ="Resource";
	private final String RESOURCES ="resources";
	private final String FILES ="files";
	private final String DATA ="/data";
	private final String FSOURCE ="FSOURCE";

	private final String URI="URI";
	private final String TARGET="TARGET";
	private final String FTARGET="FTARGET";

	private final String USER="USER";
	private final String DATE="DATE";
	
	private final String EVENT_REASON="EVENT_REASON";
	private final String OVERWRITE="OVERWRITE";

	private final String NAME="Name";

	private final String SIZE="Size";

	
	
	public TriageRestlet(Context context, Request request, Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}

	@Override
	public boolean allowGet() {
		return true;
	}

	@Override
	public boolean allowPut() {
		return true;
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public boolean allowDelete() {
		return true;
	}
	//#if($data.getSession().getAttribute("user").canEdit("xnat:projectData/ID",$project) || $data.getSession().getAttribute("user").getUsername().equals($theuser))

	
	@Override
	public void handleGet() {
		
		try {
			
	
	        String PROJECT=(String)getParameter(getRequest(),"PROJECT");
			String projectPath=TriageUtils.getTriageProjectPath(PROJECT);
	        String pXNAME = (String)getParameter(getRequest(),"XNAME");
	        String pFILE = (String)getParameter(getRequest(),"FILE");
	        
	        
	        
	        if( PROJECT==null){
	        	fail(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid Operation.");
	        	return;
	        }
	         
        	XnatProjectdata proj = XnatProjectdata.getProjectByIDorAlias(PROJECT, this.getUser(), false);
        	if(proj!=null && proj.canRead(this.getUser())){//only return data when the user can read the original project.
	        
		        if (pXNAME == null && pFILE == null) {
		        	returnXnameList(proj,projectPath+File.separator+"resources");
		        } else if (pXNAME != null && pFILE == null) {
		        	if (isZIPRequest()) {
		        		returnZippedFiles(proj,projectPath,pXNAME);
		        	} else {
		        		returnFileList(proj,projectPath,pXNAME);
		        	}
		        } else if (pXNAME != null && pFILE != null) {
		        	if (isZIPRequest()) {
		        		returnZippedFiles(proj,projectPath,pXNAME,pFILE);
		        	} else {
		        		returnFile(proj,projectPath,pXNAME,pFILE);
		        	}
		        }
        	}else{
        		fail(Status.CLIENT_ERROR_UNAUTHORIZED,"Not authorized");
        	}
        
		} catch (Exception e) {
			fail(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			logger.error("",e);
		}
    }
	
	@Override
	public void handleDelete() {
		
		try {
			String PROJECT=(String)getParameter(getRequest(),"PROJECT");
			String triagePath = TriageUtils.getTriageUploadsPath();
			String projectPath=TriageUtils.getTriageProjectPath(PROJECT);
			String pXNAME = (String)getParameter(getRequest(),"XNAME");
	        String pFILE = (String)getParameter(getRequest(),"FILE");
	        String theEVENT_REASON=(String)getParameter(getRequest(),EVENT_REASON);
	        
	        if( PROJECT==null){
	        	fail(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid Operation.");
	        	return;
	        } 
	        XnatProjectdata proj = XnatProjectdata.getProjectByIDorAlias(PROJECT, this.getUser(), false);
	        	
	        if(proj!=null && proj.canRead(this.getUser())){//only continue when the user can read the project.
	        	
		        if (pXNAME == null && pFILE == null) {
		        	fail(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid Operation.");
		        } else if (pXNAME != null && pFILE == null) {
		        	deleteTriageResource(proj,projectPath,pXNAME);
		        } else if (pXNAME != null && pFILE != null) {
		        	deleteTriageFiles(proj,projectPath,pXNAME,pFILE);
		        }
	        }else{
	        	fail(Status.CLIENT_ERROR_UNAUTHORIZED,"Not authorized");
	        }
		   
		} catch (Exception e) {
			fail(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			logger.error("",e);
		}
    }
	
	@Override
	public void handlePost() {
		
		try {
		
			String PROJECT=(String)getParameter(getRequest(),"PROJECT");
			String projectPath = TriageUtils.getTriageUploadsPath();
	        String pXNAME = (String)getParameter(getRequest(),"XNAME");
	        String pFILE = (String)getParameter(getRequest(),"FILE");
	        	        
	        if( PROJECT==null){
	        	fail(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid Operation.");
	        	return;
	        } 
	        XnatProjectdata proj = XnatProjectdata.getProjectByIDorAlias(PROJECT, this.getUser(), false);
        	if(proj!=null && proj.canRead(this.getUser()) && canEditDestination()){
		        if (pXNAME == null && pFILE == null) {
		        	fail(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid Operation.");
			    } else if (pXNAME != null && pFILE == null) {
			        uploadTriageFile(projectPath,pXNAME);
			    }else if (pXNAME != null && pFILE != null) {
			        uploadTriageFile(projectPath,this.getxName(PROJECT),pFILE);
			    }
	        	this.openworkflow(true,"Upload Quarantine Files", this.getReason(), this.getComment());
	        	
	        	//XNATCR-834: stops IE opening save dialog
	        	this.getResponse().setEntity("",MediaType.TEXT_HTML);
        	}else{
        		fail(Status.CLIENT_ERROR_UNAUTHORIZED,"Not authorized");
        	}
		} catch (Exception e) {
			fail(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			logger.error("",e);
		}
    }	
	
	
	
	
	public ResourceURII convertValue(final String key) throws ClientException{
		try {
			URIManager.DataURIA uri=UriParserUtils.parseURI(key);
			
			if(uri instanceof ResourceURII){
				return (ResourceURII)uri;
			}else{
				throw new ClientException("Invalid Destination:"+ key);
			}
		} catch (MalformedURLException e) {
			throw new ClientException("Invalid Destination:"+ key,e);
		}
	}
	public void workflow(boolean status,String action,String reason,String comment) throws Exception{
		PersistentWorkflowI work=WorkflowUtils.getOrCreateWorkflowData(this.getEventId(), this.getUser(), "xnat:projectData", (String)getParameter(getRequest(),"PROJECT"), (String)getParameter(getRequest(),"PROJECT"),EventUtils.newEventInstance(EventUtils.CATEGORY.DATA,EventUtils.TYPE.WEB_FORM, action,reason,comment));
		if(status){
			WorkflowUtils.complete(work, work.buildEvent());		
		}else{
			WorkflowUtils.fail(work, work.buildEvent());
		}
	}
	
	
	public void openworkflow(boolean status,String action,String reason,String comment) throws Exception{
		PersistentWorkflowI work=WorkflowUtils.buildOpenWorkflow( this.getUser(), "xnat:projectData", (String)getParameter(getRequest(),"PROJECT"), (String)getParameter(getRequest(),"PROJECT"),EventUtils.newEventInstance(EventUtils.CATEGORY.DATA,EventUtils.TYPE.WEB_FORM,  action,reason,comment));
		if(status){
			WorkflowUtils.complete(work, work.buildEvent());		
		}else{
			WorkflowUtils.fail(work, work.buildEvent());
		}
	}
	
	
	
	public void fail(Status status, String msg){
		this.getResponse().setStatus(status,msg);
		
		String _return =this.retrieveParam(_ON_FAILURE_RETURN_JS);
		if(_return !=null){
			getResponse().setEntity(new StringRepresentation("<script>"+_return + "</script>", MediaType.TEXT_HTML));
		}else{
			_return =this.retrieveParam(_ON_FAILURE_RETURN_HTML);
			if(_return !=null){
				getResponse().setEntity(new StringRepresentation(_return , MediaType.TEXT_HTML));
			}
		}
	}
	
	/*public void success(Status status, String msg){
		this.getResponse().setStatus(status,msg);
		
		String _return =this.retrieveParam(_ON_SUCCESS_RETURN_JS);
		if(_return !=null){
			getResponse().setEntity(new StringRepresentation("<script>"+_return + "</script>", MediaType.TEXT_HTML));
		}else{
			_return =this.retrieveParam(_ON_SUCCESS_RETURN_HTML);
			if(_return !=null){
				getResponse().setEntity(new StringRepresentation(_return , MediaType.TEXT_HTML));
			}
		}
	}*/
	
	
	@Override
	public void handlePut() {
		
		try {
			
			String PROJECT=(String)getParameter(getRequest(),"PROJECT");
			String projectPath = TriageUtils.getTriageUploadsPath();
	        String pXNAME = (String)getParameter(getRequest(),"XNAME");
	        String pFILE = (String)getParameter(getRequest(),"FILE");
	        
	        if(PROJECT==null){
	        	fail(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid Operation.");
	        	return;
	        }
	        XnatProjectdata proj = XnatProjectdata.getProjectByIDorAlias(PROJECT, this.getUser(), false);
        	if(proj!=null && proj.canRead(this.getUser()) && canEditDestination()){
	       
		        if(pXNAME == null && pFILE == null) {
		        	fail(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid Operation.");
		        }else if (pXNAME != null && pFILE == null) {
		        	createTriageResource(projectPath,pXNAME);
		        }else if (pXNAME != null && pFILE != null) {
		        	uploadTriageFile(projectPath,this.getxName(PROJECT),pFILE);
		        }
	        	this.openworkflow(true,"Upload Quarantine Files", "Upload Quarantine Files", "Upload Quarantine Files");
        	}else{
    	        	fail(Status.CLIENT_ERROR_UNAUTHORIZED,"Not authorized");
    	     }
        	
		} catch (Exception e) {
			fail(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			logger.error("",e);
		}
    }

	
	
	private void returnXnameList(XnatProjectdata proj,String projectPath) throws InvalidItemException, Exception {
	
        File[] fileArray = new File(projectPath).listFiles();
        
        ArrayList<String> columns=new ArrayList<String>();
        columns.add(RESOURCE);
        columns.add(URI);
        columns.add(TARGET);
        columns.add(USER);
        columns.add(DATE);
        columns.add(OVERWRITE);
        columns.add(EVENT_REASON);
        columns.add(FTARGET);
        columns.add(FORMAT);
        columns.add(CONTENT);
        columns.add(FSOURCE);
        
        XFTTable table=new XFTTable();
        table.initTable(columns);
        if(fileArray!=null){
	        for (File f : fileArray) {
	        	String fn=f.getName();
	        	Object[] oarray = new Object[] { fn, 
	        			constructResourceURI(fn),
	        			getPropertyFromManifest(f, TARGET),
	        			getPropertyFromManifest(f, USER),
	        			getPropertyFromManifest(f, DATE),
	        			getPropertyFromManifest(f, OVERWRITE),
	        			getPropertyFromManifest(f, EVENT_REASON),
	        			getPropertyFromManifest(f, FTARGET),
	        			getPropertyFromManifest(f, FORMAT),
	        			getPropertyFromManifest(f, CONTENT),
	        			getFileListing(f)};
	        	if(this.canRead(proj, f)){
	        		table.insertRow(oarray);
	        	}
	        }
        }
        
        sendTableRepresentation(table,true);
		
	}
	
	 
	
	String getFileListing(File resource){
		File fileDir = new File (resource.getAbsolutePath()+File.separator+"files");
		ArrayList<File> fileList = new ArrayList<File>();
		fileList.addAll(FileUtils.listFiles(fileDir,null,true));
		String names=new String();
		for (File file : fileList) {
			if(!MANIFEST.equals(file.getName()))
				names= names +file.getName()+"<br>";
		}
		return names;
	}
	
	
	
	@SuppressWarnings("unchecked")
	private void returnFileList(XnatProjectdata proj, String projectPath,String pXNAME) throws InvalidItemException, Exception {
		
		File dir = new File (projectPath+File.separator+ File.separator+"resources"+File.separator+pXNAME+File.separator+"files");
		File resoureDir=new File (projectPath+File.separator+ File.separator+"resources");
		//need to ignore .json files.
		if (dir.exists() && dir.isDirectory()) {
			
			ArrayList<File> fileList = new ArrayList<File>();
			fileList.addAll(FileUtils.listFiles(dir,null,true));
			//Implement a sorting comparator on file list: Unnecessary, it is sorted by the representTable method.
	        ArrayList<String> columns=new ArrayList<String>();
	        columns.add(NAME);
	        columns.add(SIZE);
	        columns.add(URI);
	        columns.add(TARGET);
	        columns.add(USER);
	        columns.add(DATE);
	        columns.add(OVERWRITE);
	        columns.add(EVENT_REASON);
	        columns.add(FTARGET);
	        columns.add(FORMAT);
	        columns.add(CONTENT);

	        XFTTable table=new XFTTable();
	        table.initTable(columns);
	        
	        Iterator<File> i = fileList.iterator();
	        while (i.hasNext()) {
	        	File f = i.next();
	        	//String path=constructPath(f);
	        	if(!MANIFEST.equals(f.getName())){
	        		//should be relative to dir...
	        		String fileRelativeName=this.relative(dir, f);
	        		Object[] oarray = new Object[] { fileRelativeName, f.length(), constructURI(fileRelativeName),getPropertyFromManifest(f, TARGET),getPropertyFromManifest(f, USER),getPropertyFromManifest(f, DATE),getPropertyFromManifest(f, OVERWRITE),getPropertyFromManifest(f, EVENT_REASON),getPropertyFromManifest(f, FTARGET),getPropertyFromManifest(f, FORMAT),getPropertyFromManifest(f, CONTENT)};
	        		if(this.canRead(proj, resoureDir)){
	        			table.insertRow(oarray);
	        		}
	        	}
	        }
		
	        sendTableRepresentation(table,true);
	        
		} else {
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Quarantine directory not found or is not a directory.");
		}
		
	}
	public String relative( final File base, final File file ) {
	    final int rootLength = base.getAbsolutePath().length();
	    final String absFileName = file.getAbsolutePath();
	    final String relFileName = absFileName.substring(rootLength + 1);
	    return relFileName;
	}
	
	private void returnFile(XnatProjectdata proj, String projectPath,String pXNAME,String pFILE) throws InvalidItemException, Exception {
		
		String escapedPath=projectPath+File.separator+ File.separator+"resources"+File.separator+pXNAME+File.separator+"files"+File.separator+pFILE;
		String resourcePath=projectPath+File.separator+ File.separator+"resources";
		String path=URLDecoder.decode(escapedPath, "UTF-8");
		File reqFile = new File (path);
		if (reqFile.exists() && reqFile.isFile() && canRead(proj, new File(escapedPath))) {
			sendFileRepresentation(reqFile);
		} else {
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Quarantine file not found.");
		}
	}
	
	
private void deleteTriageResource(XnatProjectdata proj ,String projectPath,String pXNAME) throws Exception {
	
	File dir = new File (projectPath+File.separator+RESOURCES+File.separator+pXNAME);
	if(this.canDelete(proj, dir)){
		if (dir.exists() && dir.isDirectory()) {
			
			try {
				FileUtils.deleteDirectory(dir);
				this.getResponse().setStatus(Status.SUCCESS_OK);
	        	this.workflow(true,"Delete Quarantine Files", this.getReason(), this.getComment());

			} catch (IOException e) {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
				logger.error("",e);
			}
		} else {
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Quarantine directory not found or is not a directory.");
		}
	}else{
        getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User account doesn't have permission to modify this resource.");

	}
	
}
	
	private void deleteTriageFiles(XnatProjectdata proj, String projectPath, String pXNAME, String pFILE) throws InvalidItemException, Exception {
		
		File fi = new File (projectPath+File.separator+ File.separator+"resources"+File.separator+pXNAME+File.separator+"files"+File.separator+pFILE);
		if(canDelete(proj, fi)){
			ArrayList<File> fileList=new ArrayList<File>();
	
	        if (fi.exists()) {
	           	fileList.add(fi);
	        }
			
			boolean deleteOK = true;
			if (fileList.size()>0) {
	            for (File f : fileList) {  
	            	if (f.isDirectory()) {
	            		try {
	            			FileUtils.deleteDirectory(f);
	            		} catch (IOException e) {
	            			deleteOK = false;
	            		}
	            	} else {
	            		if (!f.delete()) {
	            			deleteOK = false;
	            		}
	            	}
	            }
	            if (deleteOK) {
		        	this.workflow(true,"Delete Quarantine Files", this.getReason(), this.getComment());
					this.getResponse().setStatus(Status.SUCCESS_OK);
	            } else {
					this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Problem deleting one or more server files.");
	            }
			} else {
				this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"No matching files found.");
			}
		}else{
            this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User account doesn't have permission to delete this file.");
		}
	}
		
	private void createTriageResource(String projectPath,String pXNAME) {
		
		// Create any subdirectories requested as well
		String dirString = pXNAME + getRequest().getResourceRef().getRemainingPart().replaceFirst("\\?.*$", "");
		File dir = new File (projectPath,dirString);
		if (dir.exists()) {
			this.getResponse().setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED,"Resource with this name already exists.");
		} else {
			if (dir.mkdirs()) {
				this.getResponse().setStatus(Status.SUCCESS_OK);
			} else {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Could not create resource directory.");
			}
		}	
		
	}
	
	private boolean uploadTriageFile(String projectPath,String pXNAME) {
		return uploadTriageFile(projectPath,pXNAME,null);
	}
	
	
	/*
	 * manifest file for uploads...
	 *	1.Convert Java object to JSON format
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(new File("c:\\user.json"), user);
		2. Convert JSON to Java object
		ObjectMapper mapper = new ObjectMapper();
		User user = mapper.readValue(new File("c:\\user.json"), User.class); 
	 * 
	 */
	//if existing add
	//if new create.
	//dont add duplicates
	private boolean saveTriageManifestFile(String path,String name) {
		boolean success=false;
		
		File manifestFile;
		File resourceFile;
		ObjectMapper mapper = new ObjectMapper();
		try {
			
			if(StringUtils.isNotEmpty(name)){
				name = StringUtils.stripEnd(name,"/");
				resourceFile=new File(path+File.separator+name);
				manifestFile=new File(path+File.separator+MANIFEST);
			}else{
				resourceFile=new File(path);
				manifestFile=this.getManifestFileFromResource(resourceFile);
			}
			logger.warn(manifestFile.getPath());
			logger.warn(resourceFile.getPath());
			
			TriageManifest tManifest=(manifestFile.exists())?mapper.readValue(manifestFile, TriageManifest.class): new TriageManifest();
			
			Map<String, String> entry=new HashMap<String,String>();
			entry.put("Resource", constructResourceURI(resourceFile.getName()));
			entry.put("URI", constructResourceURI(resourceFile.getName()));
			entry.put("TARGET", constructTargetURI(resourceFile.getName()));
			entry.put("FTARGET", constructFormattedTargetURI(resourceFile.getName()));
			//entry.put("FSOURCE", constructResourceNames(resourceFile.getName()));

			entry.put("USER",this.getUser().getUsername());
			entry.put("DATE",(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format( Calendar.getInstance().getTime()));

			//entry.put("DATE",java.util.Calendar.getInstance(java.util.TimeZone.getDefault()).getTime().toString());
			entry.put(OVERWRITE,constructOverwrite());
			entry.put(EVENT_REASON,constructEventReason());
			
			entry.put(FORMAT,constructFormat());
			entry.put(CONTENT,constructContent());
			
			tManifest.addEntry(entry);
			mapper.writeValue(manifestFile, tManifest);
			
			
			success=true;
		} catch (JsonGenerationException e) {
			logger.warn(e.getMessage());
			success=false;
		} catch (JsonMappingException e) {
			logger.warn(e.getMessage());
			success=false;

		} catch (IOException e) {
			logger.warn(e.getMessage());
			success=false;
		}

		return success;
	}
	
	
	/*
	 * getManifestFileFromResource from the file or the resource directory name.
	 * 
	 */
	private File getManifestFileFromResource(File resourceFile){
		File manifestFile=new File(resourceFile.getParentFile().getAbsolutePath()+File.separator+MANIFEST);
		if(!manifestFile.exists()){
			manifestFile=new File(resourceFile.getAbsolutePath()+File.separator+FILES+File.separator+MANIFEST);
		}
		return manifestFile;
	}
	
	private boolean uploadTriageFile(String projectPath,String pXNAME,String pFILE) {

		// Create any subdirectories requested as well
		String fileName=null;
		File directory=null;
		File resourceFile=new File(projectPath + File.separator + pXNAME);
		fileName=resourceFile.getName();
		directory=resourceFile.getParentFile();
		logger.warn("resourceFile:"+resourceFile.getAbsolutePath());
		logger.warn("directory:"+directory.getAbsolutePath());

		
		//Upload to non-existing resources (auto-create) or fail?  Should auto-create.
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Could not create resource directory.");
				return false;
			}
		}
		
		saveTriageManifestFile(directory.getAbsolutePath(),fileName);
		
		
		if(this.isQueryVariableTrue("inbody")){
			return handleInbodyTriageFileUpload(projectPath,directory.getAbsolutePath(),fileName);
		} else {
			return handleAttachedTriageFileUpload(projectPath,directory.getAbsolutePath(),fileName);
		}
		
	}
	
	
	private boolean handleInbodyTriageFileUpload(String projectPath, String dirString, String fileName) {
		
		try {
			
			// This is probably redundant due to current doPut/doPost coding, but including it anyway.
			if (fileName==null || fileName.length()<1) {
	        	this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Please use HTTP PUT request to specify a file name in the URL.");
	        	return false;
			}
	        
	        // Write original file if not requesting or have non-archive file
			File ouf=new File(dirString,fileName);
			
			ouf.getParentFile().mkdirs();
			
			(new FileWriterWrapper(this.getRequest().getEntity(),fileName)).write(ouf);
	        
			logger.warn("fileName "+fileName);
			logger.warn("dirString "+dirString);
			

	        return true;
			
		} catch (Exception e) {
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			logger.error("",e);
			return false;
		}
		
	}
	
	private Map<String,String> bodyParams=Maps.newHashMap();
	
	private String retrieveParam(String key){
		String param=this.getQueryVariable(key);
		if(param==null){
			if(bodyParams.containsKey(key)){
				return bodyParams.get(key);
			}
		}
		
		return param;
	}
	

	@SuppressWarnings("deprecation")
	private boolean handleAttachedTriageFileUpload(String projectPath, String dirString, String requestedName) {
		
		org.apache.commons.fileupload.DefaultFileItemFactory factory = new org.apache.commons.fileupload.DefaultFileItemFactory();
		org.restlet.ext.fileupload.RestletFileUpload upload = new  org.restlet.ext.fileupload.RestletFileUpload(factory);
	
	    List<FileItem> fileItems;
		try {
			
			fileItems = upload.parseRequest(this.getRequest());
	
			for (FileItem fi:fileItems) {    						         
		    	
				if (fi.isFormField()) {
                	// Load form field to passed parameters map
					bodyParams.put(fi.getFieldName(),fi.getString());
                   	continue;
                } 
				
		        String fileName;
				if (requestedName==null || requestedName.length()<1) {
					fileName=fi.getName();
				} else {
					fileName=requestedName;
				}
				
				final String extract=this.retrieveParam("extract");
		        /*if (extract!=null && extract.equalsIgnoreCase("true")) {
		        	// Write extracted files
		        	CompressionMethod method = getCompressionMethod(fileName);
		        	if (method != CompressionMethod.NONE) {
		        		if (!extractCompressedFile(fi.getInputStream(),dirString,fileName,method)) {
		        			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Error extracting file.");
		        			return false;
		        		} else {
		        			// If successfully extracted, don't create unextracted file
		        			continue;
		        		}
		        	}
		        }*/
	        	fi.write(new File(dirString + "/" + fileName));
			
		    }
			return true;
	    
		} catch (Exception e) {
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			logger.error("",e);
			return false;
		}
		
	}

	private boolean extractCompressedFile(InputStream is,String dirString,String fileName,CompressionMethod method) throws IOException {
	
       ZipI zipper = null;
       if (method == CompressionMethod.TAR) {
           zipper = new TarUtils();
       } else if (method == CompressionMethod.GZ) {
           zipper = new TarUtils();
           zipper.setCompressionMethod(ZipOutputStream.DEFLATED);
       } else {
           zipper = new ZipUtils();
       }
	
	   try {
	    	zipper.extract(is,dirString);
	   } catch (Exception e) {
		   
			this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,"FILE:  " + fileName +
							" - Archive file is corrupt or not a valid archive archive file type.");
			return false;
	   }
	   return true;
		
	}

	private CompressionMethod getCompressionMethod(String fileName) {
		
		// Assume file name represents correct compression method
        String file_extension = null;
        if (fileName.indexOf(".")!=-1) {
        	file_extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        	if (Arrays.asList(zipExtensions).contains(file_extension)) {
	        	return CompressionMethod.ZIP;
	        } else if (file_extension.equalsIgnoreCase(".tar")) {
	        	return CompressionMethod.TAR;
	        } else if (file_extension.equalsIgnoreCase(".gz")) {
	        	return CompressionMethod.GZ;
	        }
        }
        return CompressionMethod.NONE;
        
	}

	private void sendTableRepresentation(XFTTable table,boolean containsURI) {
        
		MediaType mt = overrideVariant(this.getPreferredVariant());
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		if (table != null) {
			params.put("totalRecords", table.size());
		}
		
		Map<String,Map<String,String>> cp = new Hashtable<String,Map<String,String>>();
		if (containsURI) {
			cp.put(URI, new Hashtable<String,String>());
			String rootPath = this.getRequest().getRootRef().getPath();
			if(rootPath.endsWith("/data")){
				rootPath=rootPath.substring(0,rootPath.indexOf("/data"));
			}
			if(rootPath.endsWith("/REST")){
				rootPath=rootPath.substring(0,rootPath.indexOf("/REST"));
			}
			cp.get(URI).put("serverRoot", rootPath);
		}
		if (containsURI) {
			cp.put(TARGET, new Hashtable<String,String>());
			String rootPath = this.getRequest().getRootRef().getPath();
			if(rootPath.endsWith("/data")){
				rootPath=rootPath.substring(0,rootPath.indexOf("/data"));
			}
			if(rootPath.endsWith("/REST")){
				rootPath=rootPath.substring(0,rootPath.indexOf("/REST"));
			}
			cp.get(TARGET).put("serverRoot", rootPath);
		}
		getResponse().setEntity(this.representTable(table, mt, params, cp));
		
	}
	
	// TODO :  Move this (and corresponding one in FileList) to Utils class 
	private MediaType getMediaType(String fileName) {
		
		MediaType mt = overrideVariant(this.getPreferredVariant());
		if(fileName.endsWith(".gif")){
			mt = MediaType.IMAGE_GIF;
		}else if(fileName.endsWith(".jpeg")){
			mt = MediaType.IMAGE_JPEG;
		}else if(fileName.endsWith(".xml")){
			mt = MediaType.TEXT_XML;
		}else if(fileName.endsWith(".jpg")){
			mt = MediaType.IMAGE_JPEG;
		}else if(fileName.endsWith(".png")){
			mt = MediaType.IMAGE_PNG;
		}else if(fileName.endsWith(".bmp")){
			mt = MediaType.IMAGE_BMP;
		}else if(fileName.endsWith(".tiff")){
			mt = MediaType.IMAGE_TIFF;
		}else if(fileName.endsWith(".html")){
			mt = MediaType.TEXT_HTML;
		}else{
			if(mt.equals(MediaType.TEXT_XML) && !fileName.endsWith(".xml")){
				mt=MediaType.ALL;
			}else{
				mt=MediaType.APPLICATION_OCTET_STREAM;
			}
		}
		return mt;
		
	}
	
	private void sendFileRepresentation(File f) {
		
		MediaType mt = getMediaType(f.getName());
		this.setResponseHeader("Cache-Control", "must-revalidate");
		getResponse().setEntity(this.representFile(f, mt));
		
	}
	//"/services/triage/projects/{PROJECT}/subjects/{SUBJECT}/experiments/{EXPERIMENT}/assessors/{ASSESSORS}/resources/{XNAME}/files/{FILE}"

	String getxName(String project){
		String requestPart = this.mapToProjectResources(this.getHttpServletRequest().getServletPath() + this.getHttpServletRequest().getPathInfo());

		int bindex =StringUtils.indexOf(requestPart, "/projects/"+project);
		//only compress urls for subject and deeper
		int lindex =StringUtils.lastIndexOf(requestPart, "/resources");
		String xName=requestPart.substring(bindex);
		//mapped=originalResourceUrl.replace(extra, "");
		
		return xName;
	}
	String mapToProjectResources(String originalResourceUrl){
		String mapped=originalResourceUrl;
		int bindex =StringUtils.indexOf(originalResourceUrl, "/subjects/");
		//only compress urls for subject and deeper
		if(bindex>0){
			int lindex =StringUtils.lastIndexOf(originalResourceUrl, "/resources");
			String extra=originalResourceUrl.substring(bindex, lindex);
			mapped=originalResourceUrl.replace(extra, "");
		}
		return originalResourceUrl;
	}
	
    private String constructResourceURI(String resource) {
    	String requestPart = this.mapToProjectResources(this.getHttpServletRequest().getServletPath() + this.getHttpServletRequest().getPathInfo());
    	
    	if (!requestPart.endsWith(resource)){
    		requestPart+=File.separator+resource;
    	}
    	return requestPart;// + (requestPart.endsWith("/")?"":"/") + resource;

    }
    
    private String appendFiles(String requestPart,String resource){
    	if (!requestPart.endsWith(resource) && !requestPart.endsWith("/files") && !requestPart.endsWith("/files/")) {
    		requestPart+="/files";
    	}
    	if (!requestPart.endsWith(resource)){
    		requestPart+=File.separator+resource;
    	}
    	return requestPart;
    }
    private String constructURI(String resource) {
    	String requestPart = this.mapToProjectResources(this.getHttpServletRequest().getServletPath() + this.getHttpServletRequest().getPathInfo());
 
    	return this.appendFiles(requestPart, resource);
    	
    }
    /*
     * TargetResource should be the entire uri up to RESOURCENAME. Remove excess.
     */
    private String constructTargetResourceURI(String resource) {
    	String requestPart = this.mapToProjectResources(this.getHttpServletRequest().getServletPath() + this.getHttpServletRequest().getPathInfo());
    	
    	if(requestPart.contains(FILES)){
    		int pointcut=requestPart.indexOf(FILES);
    		requestPart=requestPart.substring(0,pointcut-2);
    	}
    	return requestPart;// + (requestPart.endsWith("/")?"":"/") + resource;

    }
    private String constructOverwrite() {
     	
     	 
    	String overwrite=this.retrieveParam(OVERWRITE);
	    if (StringUtils.isEmpty(overwrite)) {
	    	overwrite="false";
	    }
	   
	    return overwrite;
     }
    private String constructContent() {
     	
      	 
    	String content=this.retrieveParam(CONTENT);
	    if (StringUtils.isEmpty(content)) {
	    	content="";
	    }
	   
	    return content;
     }
    private String constructFormat() {
     	
   	 
    	String format=this.retrieveParam(FORMAT);
	    if (StringUtils.isEmpty(format)) {
	    	format="";
	    }
	   
	    return format;
     }
    private String constructEventReason( ) {
      	
       	 
     	String event_reason=this.retrieveParam("event_reason");
 	    if (StringUtils.isEmpty(event_reason)) {
 	    	event_reason="";
 	    }
 	   
 	    return event_reason;
      }
    /*
    
     */
     private String constructTargetURI(String resource) {
     	
    	 
    	String target=this.retrieveParam("target");
	    if (StringUtils.isEmpty(target)) {
	    	//construct target from url
	    	String requestPart = this.getHttpServletRequest().getServletPath() + this.getHttpServletRequest().getPathInfo();
	    	requestPart= this.appendFiles(requestPart, resource);
	    	//replace triage with archive
	    	target=requestPart.replace(TRIAGE, ARCHIVE);
	    }
	    if(!target.startsWith(DATA)){
	    	target=target.substring(target.indexOf(DATA));
	    }
	    if(!target.endsWith(FILES)){
	    	target=target+File.separator+FILES;
	    }
	    return target;
     }
    
     public  String constructFormattedTargetURI(String resource) {
    	 String formatted=this.constructTargetURI(resource);
    	 //need to format IDs to Labels for user.
    	
    	 String[] params = StringUtils.split(formatted, "/");
    	 for(int i=0;i<params.length-1;i++){
    		 if("experiments".equals(params[i])){
    			 String expid=params[i+1];
    			 XnatExperimentdata exp=XnatExperimentdata.getXnatExperimentdatasById(expid, this.getUser(), false);
    			 if(exp!=null){
    		    	 formatted=formatted.replace(expid,exp.getLabel());
    			 }
    		 }
    		 if("subjects".equals(params[i])){
    			 String subjid=params[i+1];
    			 XnatSubjectdata subj=XnatSubjectdata.getXnatSubjectdatasById(subjid, this.getUser(), false);
    			 if(subj!=null){
    		    	 formatted=formatted.replace(subjid,subj.getLabel());
    			 }
    		 }
    		 if("assessors".equals(params[i])){
    			 String assid=params[i+1];
    			 XnatExperimentdata exp=XnatExperimentdata.getXnatExperimentdatasById(assid, this.getUser(), false);
    			 if(exp!=null){
    		    	 formatted=formatted.replace(assid,exp.getLabel());
    			 }
    		 }
    	 }
	   
    	 formatted=formatted.replace("/data/archive/projects/","<b>Project</b>: ");
    	 formatted=formatted.replace("/subjects/","<br><b>Subject</b>: ");
    	 formatted=formatted.replace("/data/archive/experiments/","<b>Session</b>: ");
    	 formatted=formatted.replace("/assessors/","<br><b>Assessor</b>: ");
    	 formatted=formatted.replace("/scans/","<br><b>Scan</b>: ");
    	 formatted=formatted.replace("/resources/","<br><b>Resource</b>: ");
    	 formatted=formatted.replace("/resources","<br><b>Resource</b>: ");
    	 formatted=formatted.replace("/files/","");
    	 formatted=formatted.replace("/files","");
    	
    	 return formatted;
     }
     
     
     public static String constructPath(File f){
     	String filePart = f.getAbsolutePath().replace(ArcSpecManager.GetInstance().getGlobalCachePath(),"");
     	filePart = filePart.replaceFirst("^[^\\\\/]+[\\\\/][^\\\\/]+[\\\\/][^\\\\/]+","");
     	return filePart;
     }
     
    private String getPropertyFromManifest(File resourceFile, String prop) {
    	ObjectMapper mapper = new ObjectMapper();
    	String target="";
    	try {
    		File manifestFile=this.getManifestFileFromResource(resourceFile);
			TriageManifest tManifest=mapper.readValue(manifestFile, TriageManifest.class);
			target=tManifest.getFirstMatchingEntry(prop);			
    	} catch (JsonParseException e) {
			logger.warn(e.getMessage());
		} catch (JsonMappingException e) {
			logger.warn(e.getMessage());
		} catch (IOException e) {
			logger.warn(e.getMessage());
		}

    	return target;

    }
	
    
	@SuppressWarnings("unchecked")
	private void returnZippedFiles(XnatProjectdata xproj,String projectPath, String pXNAME, String pFILE) throws InvalidItemException, Exception {
		ArrayList<File> fileList=new ArrayList<File>();
		String zipFileName; 
		String fileString = pFILE + getRequest().getResourceRef().getRemainingPart().replaceFirst("\\?.*$", "");
		String resourcePath=projectPath+File.separator+RESOURCES+File.separator+pXNAME;
		if(canRead(xproj, new File(resourcePath))){
			if (fileString.contains(",")) {
				String[] fileArr = fileString.split(",");
	            for (String s : fileArr) {  
	            	File f = new File(new File(projectPath,pXNAME),s);
	            	if (f.exists() && f.isDirectory()) {
	            		fileList.addAll(FileUtils.listFiles(f,null,true));
	            	} else if (f.exists()) {
	            		fileList.add(f);
	            	}
	            }
			    zipFileName = pXNAME;
			} else {
				File f = new File(new File(projectPath,pXNAME),fileString);
	           	if (f.exists() && f.isDirectory()) {
	           		fileList.addAll(FileUtils.listFiles(f,null,true));
	           	} else if (f.exists() && this.canRead(xproj, f)) {
	           		fileList.add(f);
	           	}
			    zipFileName = fileString.replaceFirst("^.*[\\\\/]", "");
			}
			if (fileList.size()>0) {
				sendZippedFiles(projectPath,pXNAME,zipFileName,fileList);
			} else {
				this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"No matching files found.");
			}
		}else {
			this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Not authorized.");
		}
		
	}

	
	@SuppressWarnings("unchecked")
	private void returnZippedFiles(XnatProjectdata xproj,String projectPath, String pXNAME) throws InvalidItemException, Exception {
		String dirPath = File.separator+"resources"+File.separator+pXNAME+File.separator+"files";
		String resourcePath=projectPath+File.separator+RESOURCES+File.separator+pXNAME;
		if(canRead(xproj, new File(resourcePath))){
			File dir = new File (projectPath,dirPath);
			if (dir.exists() && dir.isDirectory()) {
				ArrayList<File> fileList = new ArrayList<File>();
				fileList.addAll(FileUtils.listFiles(dir,null,true));
				sendZippedFiles(projectPath,pXNAME,pXNAME,fileList);
			} else {
				this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Quarantine directory not found or is not a directory.");
			}
		}else{
			this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Not authorized");
		}
		
	}
	
	private void sendZippedFiles(String projectPath,String pXNAME,String fileName,ArrayList<File> fileList) throws ActionException {
		
		ZipRepresentation zRep;
		if(getRequestedMediaType()!=null && getRequestedMediaType().equals(MediaType.APPLICATION_GNU_TAR)){
			zRep = new ZipRepresentation(MediaType.APPLICATION_GNU_TAR,projectPath,ZipOutputStream.DEFLATED);
			this.setContentDisposition(String.format("%s.tar.gz", fileName));
		}else if(getRequestedMediaType()!=null && getRequestedMediaType().equals(MediaType.APPLICATION_TAR)){
			zRep = new ZipRepresentation(MediaType.APPLICATION_TAR,projectPath,ZipOutputStream.STORED);
			this.setContentDisposition(String.format("%s.tar.gz", fileName));
		}else{
			zRep = new ZipRepresentation(MediaType.APPLICATION_ZIP,projectPath,identifyCompression(null));
			this.setContentDisposition(String.format("%s.zip", fileName));
		}
		zRep.addAllAtRelativeDirectory(projectPath,fileList);
		this.getResponse().setEntity(zRep);
		
	}
	
	public DataURIA convertKey(final String key) throws ClientException{
		try {
			
			URIManager.DataURIA uri=UriParserUtils.parseURI(key);
			
			if(uri instanceof URIManager.TriageURI){
				return (URIManager.TriageURI)uri;
			}else{
				throw new ClientException("Invalid Source:"+ key);
			}
		} catch (MalformedURLException e) {
			throw new ClientException("Invalid Source:"+ key,e);
		}
	}
	/***
	 * Find the username from the triage manifest file.
	 * 
	 * @param file
	 * @return
	 */
	String getUser(File file){
		String username="";
		File manifest=new File(file+File.separator+FILES+File.separator+MANIFEST);
		if(file.isFile()){
			username=this.getPropertyFromManifest(file, USER);
		}else{
			username=this.getPropertyFromManifest(manifest, USER);
		}
		return username;
	}
	
	boolean canDelete(XnatProjectdata proj,File file) throws InvalidItemException, Exception{
        boolean allowed=false;
        if (Features.checkFeature(this.getUser(),proj.getSecurityTags().getHash().values(), "QuarantineReview") || StringUtils.equals(this.getUser().getUsername(),this.getUser(file))){
            allowed= true;
        }else{
        	allowed= false;
        }
		return allowed;
        
	}
	
	boolean canRead(XnatProjectdata proj,File f) throws InvalidItemException, Exception{
		boolean allowed=false;
		if (Features.checkFeature(this.getUser(),proj.getSecurityTags().getHash().values(), "QuarantineReview") || StringUtils.equals(this.getUser().getUsername(),this.getUser(f))){
            allowed= true;
        }else{
        	allowed= false;
        }
		return allowed;
	}
	
	private boolean canEditDestination() throws Exception {
		String target = this.retrieveParam("target");
		String targetResource=target+"/files";
		ResourceURII arcURI=convertValue(targetResource);
		boolean authorized=false;
			if(arcURI.getSecurityItem().canEdit(this.getUser())){
				authorized=true;
			}else{
				authorized=false;
			}
		return authorized;
		
	}
}