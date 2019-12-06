/*
 * web: org.nrg.xnat.restlet.resources.UserCacheResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.services.cache.UserDataCache;
import org.nrg.xft.XFTTable;
import org.nrg.xft.utils.zip.TarUtils;
import org.nrg.xft.utils.zip.ZipI;
import org.nrg.xft.utils.zip.ZipUtils;
import org.nrg.xnat.helpers.FileWriterWrapper;
import org.nrg.xnat.restlet.representations.ZipRepresentation;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipOutputStream;

@Slf4j
public class UserCacheResource extends SecureResource {
	private static final String _ON_SUCCESS_RETURN_JS = "_onSuccessReturnJS";
	private static final String _ON_FAILURE_RETURN_JS = "_onFailureReturnJS";
	
	private static final String _ON_SUCCESS_RETURN_HTML = "_onSuccessReturnHTML";
	private static final String _ON_FAILURE_RETURN_HTML = "_onFailureReturnHTML";

	private enum CompressionMethod { ZIP, TAR, GZ, NONE }
	
	public UserCacheResource(Context context, Request request, Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_HTML));
		_globalCachePath = Paths.get(XDAT.getSiteConfigPreferences().getCachePath());
		_userDataCache = XDAT.getContextService().getBean(UserDataCache.class);
		_userPath = _userDataCache.getUserDataCache(getUser()).toAbsolutePath().toString();
		_pXname = (String)getParameter(getRequest(),"XNAME");
		_pFile = (String)getParameter(getRequest(),"FILE");
		_hasPXname = StringUtils.isNotBlank(_pXname);
		_hasPFile = StringUtils.isBlank(_pFile);
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
	
	@Override
	public void handleGet() {
		try {
	        if (!_hasPXname && !_hasPFile) {
	        	returnXnameList(_userPath);
	        } else if (_hasPXname && !_hasPFile) {
	        	if (isZIPRequest()) {
	        		returnZippedFiles(_userPath, _pXname);
	        	} else {
	        		returnFileList(_userPath, _pXname);
	        	}
	        } else if (_hasPXname) {
	        	if (isZIPRequest()) {
	        		returnZippedFiles(_userPath, _pXname, _pFile);
	        	} else {
	        		returnFile(_userPath, _pXname, _pFile);
	        	}
	        }
		} catch (Exception e) {
			fail(Status.SERVER_ERROR_INTERNAL, e.getMessage());
			log.error("", e);
		}
    }
	
	@Override
	public void handleDelete() {
		try {
			if (!_hasPXname && !_hasPFile) {
	        	fail(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid Operation.");
			} else if (_hasPXname && !_hasPFile) {
	        	deleteUserResource(_userPath, _pXname);
			} else if (_hasPXname) {
        		deleteUserFiles(_userPath, _pXname, _pFile);
	        }
		} catch (Exception e) {
			fail(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			log.error("", e);
		}
    }
	
	@Override
	public void handlePost() {
		if (!_hasPXname && !_hasPFile) {
	    	fail(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid Operation.");
		} else if (_hasPXname && !_hasPFile) {
	       	if (isQueryVariableTrue("inbody")) {
	       		fail(Status.CLIENT_ERROR_BAD_REQUEST,"Please use HTTP PUT request to specify a file name in the URL.");
	       	} else if (uploadUserFile(_userPath, _pXname)) {
	       		success(Status.SUCCESS_CREATED,"File(s) successfully uploaded");
	       	} else {
	       		fail(Status.SERVER_ERROR_INTERNAL,"Unable to complete upload.");
	       	}
		} else if (_hasPXname) {
	    	fail(Status.CLIENT_ERROR_BAD_REQUEST,"Please use HTTP PUT request to specify a file name in the URL.");
	    }
    }
	
	public void fail(final Status status, final String message){
		setResponseAttributes(status, message, _ON_FAILURE_RETURN_JS, _ON_FAILURE_RETURN_HTML);
	}

	public void success(Status status, String msg){
		setResponseAttributes(status, msg, _ON_SUCCESS_RETURN_JS, _ON_SUCCESS_RETURN_HTML);
	}
	
	@Override
	public void handlePut() {
		try {
	        if (StringUtils.isAllBlank(_pXname, _pFile)) {
	        	fail(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid Operation.");
	        } else if (StringUtils.isNotBlank(_pXname) && StringUtils.isBlank(_pFile)) {
	        	createUserResource(_userPath, _pXname);
	        } else if (StringUtils.isNotBlank(_pXname)) {
//	        	commenting this out because we currently need this feature.
//	        	if (this.isQueryVariableTrue("extract")) {
//	        		// PUT Specification wants to enable a GET request on the same URL.  Wouldn't want to put extracted files without the 
//	        		// original archive file, or would't want to create and delete it.  Could possibly enable extraction by create, then 
//	        		// extracting the archive file without removing it.
//	        		fail(Status.CLIENT_ERROR_BAD_REQUEST,"File extraction not supported under HTTP PUT requests.");
//	        		return;
//	        	} 
	        	uploadUserFile(_userPath, _pXname, _pFile);
	        }
		} catch (Exception e) {
			fail(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			log.error("", e);
		}
    }

	private void setResponseAttributes(final Status status, final String msg, final String returnJs, final String returnHtml) {
		getResponse().setStatus(status, msg);

		final String js = retrieveParam(returnJs);
		if (StringUtils.isNotBlank(js)) {
			getResponse().setEntity(new StringRepresentation("<script>" + js + "</script>", MediaType.TEXT_HTML));
		} else {
			final String html = retrieveParam(returnHtml);
			if (StringUtils.isNotBlank(html)) {
				getResponse().setEntity(new StringRepresentation(html, MediaType.TEXT_HTML));
			}
		}
	}

	private void returnXnameList(String userPath) {
        File[] fileArray = new File(userPath).listFiles();
        ArrayList<String> columns= new ArrayList<>();
        columns.add("Resource");
        columns.add("URI");
        XFTTable table=new XFTTable();
        table.initTable(columns);
        if(fileArray!=null){
        for (File f : fileArray) {
        	String fn=f.getName();
        	Object[] oarray = new Object[] { fn, constructResourceURI(fn) };
        	table.insertRow(oarray);
        }
        }
        
        sendTableRepresentation(table,true);
		
	}
	
	// TODO - Make recursive list optional?
	private void returnFileList(String userPath,String pXNAME) {
		
		File dir = new File (userPath,pXNAME);
		
		if (dir.exists() && dir.isDirectory()) {

			final List<File> fileList = new ArrayList<>(FileUtils.listFiles(dir, null, true));
			//Implement a sorting comparator on file list: Unnecessary, it is sorted by the representTable method.

	        XFTTable table=new XFTTable();
	        table.initTable(COLUMNS);

			for (final File file : fileList) {
	        	String path=constructPath(file);
	        	table.insertRow(new Object[] { path.substring(1), file.length(), constructURI(path) });
	        }
		
	        sendTableRepresentation(table,true);
	        
		} else {
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"User directory not found or is not a directory.");
		}
		
	}
	
	private void returnFile(String userPath,String pXNAME,String pFILE) {
		
		File reqFile = new File(new File(userPath,pXNAME),pFILE + getRequest().getResourceRef().getRemainingPart().replaceFirst("\\?.*$", ""));
		if (reqFile.exists() && reqFile.isFile()) {
			sendFileRepresentation(reqFile);
		} else {
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"User directory not found or is not a directory.");
		}
		
	}
	
	private void deleteUserResource(String userPath,String pXNAME) {
		
		File dir = new File (userPath,pXNAME);
		
		if (dir.exists() && dir.isDirectory()) {
			
			try {
				FileUtils.deleteDirectory(dir);
				this.getResponse().setStatus(Status.SUCCESS_OK);
			} catch (IOException e) {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
				log.error("", e);
			}
	        
		} else {
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"User directory not found or is not a directory.");
		}
		
	}
	
	private void deleteUserFiles(String userPath, String pXNAME, String pFILE) {
		ArrayList<File> fileList= new ArrayList<>();
		String fileString = pFILE + getRequest().getResourceRef().getRemainingPart().replaceFirst("\\?.*$", "");
		
		if (fileString.contains(",")) {
			String[] fileArr = fileString.split(",");
            for (String s : fileArr) {  
            	File f = new File(new File(userPath,pXNAME),s);
            	if (f.exists()) {
            		fileList.add(f);
            	} else {
            		this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"One or more specified files do not exist.");
            		return;
            	}
            }
		} else {
			File f = new File(new File(userPath,pXNAME),fileString);
           	if (f.exists()) {
           		fileList.add(f);
           	}
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
				this.getResponse().setStatus(Status.SUCCESS_OK);
            } else {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Problem deleting one or more server files.");
            }
		} else {
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"No matching files found.");
		}
		
	}
	
	private void createUserResource(String userPath,String pXNAME) {
		
		// Create any subdirectories requested as well
		String dirString = pXNAME + getRequest().getResourceRef().getRemainingPart().replaceFirst("\\?.*$", "");
		File dir = new File (userPath,dirString);
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
	
	private boolean uploadUserFile(String userPath,String pXNAME) {
		return uploadUserFile(userPath,pXNAME,null);
	}
		
	
	private boolean uploadUserFile(String userPath,String pXNAME,String pFILE) {
		
		// Create any subdirectories requested as well
		String dirString;
		String fileName=null;
		String remainingPart = getRequest().getResourceRef().getRemainingPart().replaceFirst("\\?.*$", "");
		if ((pFILE == null || pFILE.length()<1) && !remainingPart.equals("files")) {
			dirString = userPath + File.separator + pXNAME + getRequest().getResourceRef().getRemainingPart().replaceFirst("\\?.*$", "");
		} else {
			dirString = userPath + File.separator + pXNAME;
			if (pFILE==null || pFILE.length()>0) {
				fileName=pFILE + File.separator + remainingPart;
			}
		}
		File dir = new File (dirString);
		//Upload to non-existing resources (auto-create) or fail?  Should auto-create.
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Could not create resource directory.");
				return false;
			}
		}
		
		
		
		if(this.isQueryVariableTrue("inbody")){
			return handleInbodyUserFileUpload(dirString, fileName);
		} else {
			return handleAttachedUserFileUpload(dirString, fileName);
		}
		
	}
	
	
	private boolean handleInbodyUserFileUpload(String dirString, String fileName) {
		
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
	        
	        return true;
			
		} catch (Exception e) {
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			log.error("", e);
			return false;
		}
		
	}

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
	private boolean handleAttachedUserFileUpload(String dirString, String requestedName) {
		
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
		        if (extract!=null && extract.equalsIgnoreCase("true")) {
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
		        } 
	        	fi.write(new File(dirString + "/" + fileName));
			
		    }
			return true;
	    
		} catch (Exception e) {
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			log.error("", e);
			return false;
		}
		
	}

	private boolean extractCompressedFile(InputStream is,String dirString,String fileName,CompressionMethod method) {
	
       final ZipI zipper;
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
        if (fileName.contains(".")) {
			final String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        	if (Arrays.asList(XDAT.getSiteConfigPreferences().getZipExtensionsAsArray()).contains(extension)) {
	        	return CompressionMethod.ZIP;
	        } else if (extension.equalsIgnoreCase(".tar")) {
	        	return CompressionMethod.TAR;
	        } else if (extension.equalsIgnoreCase(".gz")) {
	        	return CompressionMethod.GZ;
	        }
        }
        return CompressionMethod.NONE;
        
	}

	private void sendTableRepresentation(XFTTable table, final boolean containsURI) {
        
		MediaType mt = overrideVariant(this.getPreferredVariant());
		Hashtable<String, Object> params = new Hashtable<>();
		if (table != null) {
			params.put("totalRecords", table.size());
		}
		
		Map<String,Map<String,String>> cp = new Hashtable<>();
		if (containsURI) {
			cp.put("URI", new Hashtable<String,String>());
			String rootPath = this.getRequest().getRootRef().getPath();
			if(rootPath.endsWith("/data")){
				rootPath=rootPath.substring(0,rootPath.indexOf("/data"));
			}
			if(rootPath.endsWith("/REST")){
				rootPath=rootPath.substring(0,rootPath.indexOf("/REST"));
			}
			cp.get("URI").put("serverRoot", rootPath);
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
			
    private String constructResourceURI(String resource) {
    	
    	String requestPart = this.getHttpServletRequest().getServletPath() + this.getHttpServletRequest().getPathInfo();
    	return requestPart + (requestPart.endsWith("/")?"":"/") + resource;

    }
			
    private String constructURI(String path) {
    	
    	String requestPart = this.getHttpServletRequest().getServletPath() + this.getHttpServletRequest().getPathInfo();
    	if (requestPart.endsWith("/resources/files") || !requestPart.endsWith("/files")) {
    		requestPart+="/files";
    	}
    	
    	return requestPart + path;
    	
    }
    
    public static String constructPath(File f){

    	String filePart = f.getAbsolutePath().replace(ArcSpecManager.GetInstance().getGlobalCachePath(),"");
    	filePart = filePart.replaceFirst("^[^\\\\/]+[\\\\/][^\\\\/]+[\\\\/][^\\\\/]+","");
    	return filePart;
    }
	
	private void returnZippedFiles(String userPath, String pXNAME, String pFILE) throws ActionException {
		ArrayList<File> fileList= new ArrayList<>();
		String zipFileName; 
		String fileString = pFILE + getRequest().getResourceRef().getRemainingPart().replaceFirst("\\?.*$", "");
		
		if (fileString.contains(",")) {
			String[] fileArr = fileString.split(",");
            for (String s : fileArr) {  
            	File f = new File(new File(userPath,pXNAME),s);
            	if (f.exists() && f.isDirectory()) {
            		fileList.addAll(FileUtils.listFiles(f,null,true));
            	} else if (f.exists()) {
            		fileList.add(f);
            	}
            }
		    zipFileName = pXNAME;
		} else {
			File f = new File(new File(userPath,pXNAME),fileString);
           	if (f.exists() && f.isDirectory()) {
           		fileList.addAll(FileUtils.listFiles(f,null,true));
           	} else if (f.exists()) {
           		fileList.add(f);
           	}
		    zipFileName = fileString.replaceFirst("^.*[\\\\/]", "");
		}
		if (fileList.size()>0) {
			sendZippedFiles(userPath,pXNAME,zipFileName,fileList);
		} else {
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"No matching files found.");
		}
		
	}

	private void returnZippedFiles(String userPath, String pXNAME) throws ActionException {
		
		File dir = new File (userPath,pXNAME);
		if (dir.exists() && dir.isDirectory()) {
			ArrayList<File> fileList = new ArrayList<>(FileUtils.listFiles(dir,null,true));
			sendZippedFiles(userPath,pXNAME,pXNAME,fileList);
		} else {
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"User directory not found or is not a directory.");
		}
		
	}
	
	private void sendZippedFiles(String userPath,String pXNAME,String fileName,List<File> fileList) throws ActionException {
		
		ZipRepresentation zRep;
		if(getRequestedMediaType()!=null && getRequestedMediaType().equals(MediaType.APPLICATION_GNU_TAR)){
			zRep = new ZipRepresentation(MediaType.APPLICATION_GNU_TAR,userPath,ZipOutputStream.DEFLATED);
			this.setContentDisposition(String.format("%s.tar.gz", fileName));
		}else if(getRequestedMediaType()!=null && getRequestedMediaType().equals(MediaType.APPLICATION_TAR)){
			zRep = new ZipRepresentation(MediaType.APPLICATION_TAR,userPath,ZipOutputStream.STORED);
			this.setContentDisposition(String.format("%s.tar.gz", fileName));
		}else{
			zRep = new ZipRepresentation(MediaType.APPLICATION_ZIP,userPath,identifyCompression(null));
			this.setContentDisposition(String.format("%s.zip", fileName));
		}
		zRep.addAllAtRelativeDirectory(userPath + File.separator + pXNAME,fileList);
		this.getResponse().setEntity(zRep);
		
	}

	private static final ArrayList<String> COLUMNS = new ArrayList<>(Arrays.asList("Name", "Size", "URI"));

	private Map<String,String> bodyParams = new HashMap<>();

	private final UserDataCache _userDataCache;
	private final Path          _globalCachePath;
	private final String        _userPath;
	private final String        _pXname;
	private final String        _pFile;
	private final boolean _hasPXname;
	private final boolean _hasPFile;

}
