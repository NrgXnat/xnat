/*
 * web: org.nrg.xnat.restlet.resources.UserCacheResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import com.google.common.collect.Maps;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.nrg.action.ActionException;
import org.nrg.xdat.XDAT;
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
import java.util.*;
import java.util.zip.ZipOutputStream;

public class UserCacheResource extends SecureResource {
	private static final String _ON_SUCCESS_RETURN_JS = "_onSuccessReturnJS";
	private static final String _ON_FAILURE_RETURN_JS = "_onFailureReturnJS";
	
	private static final String _ON_SUCCESS_RETURN_HTML = "_onSuccessReturnHTML";
	private static final String _ON_FAILURE_RETURN_HTML = "_onFailureReturnHTML";

	static Logger logger = Logger.getLogger(UserCacheResource.class);

	private enum CompressionMethod { ZIP, TAR, GZ, NONE }
	
	public UserCacheResource(Context context, Request request, Response response) {
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
	
	@Override
	public void handleGet() {
		
		try {
			
			String userPath = org.nrg.xdat.security.helpers.Users.getUserCacheUploadsPath(getUser());
	        String pXNAME = (String)getParameter(getRequest(),"XNAME");
	        String pFILE = (String)getParameter(getRequest(),"FILE");
	        
	        if (pXNAME == null && pFILE == null) {
	        	
	        	returnXnameList(userPath);
	        	
	        } else if (pXNAME != null && pFILE == null) {
	        	
	        	if (isZIPRequest()) {
	        		returnZippedFiles(userPath,pXNAME);
	        	} else {
	        		returnFileList(userPath,pXNAME);
	        	}
	        	
	        } else if (pXNAME != null && pFILE != null) {
	        	
	        	if (isZIPRequest()) {
	        		returnZippedFiles(userPath,pXNAME,pFILE);
	        	} else {
	        		returnFile(userPath,pXNAME,pFILE);
	        	}
	        	
	        }
        
		} catch (Exception e) {
			fail(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			logger.error("",e);
		}
    }
	
	@Override
	public void handleDelete() {
		
		try {
			
			String userPath = org.nrg.xdat.security.helpers.Users.getUserCacheUploadsPath(getUser());
	        String pXNAME = (String)getParameter(getRequest(),"XNAME");
	        String pFILE = (String)getParameter(getRequest(),"FILE");
	        
	        if (pXNAME == null && pFILE == null) {
	        	
	        	fail(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid Operation.");
	        	
	        } else if (pXNAME != null && pFILE == null) {
	        	
	        	deleteUserResource(userPath,pXNAME);
	        	
	        } else if (pXNAME != null && pFILE != null) {
	        	
        		deleteUserFiles(userPath,pXNAME,pFILE);
	        	
	        }
        
		} catch (Exception e) {
			fail(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			logger.error("",e);
		}
    }
	
	@Override
	public void handlePost() {
		
		String userPath = org.nrg.xdat.security.helpers.Users.getUserCacheUploadsPath(getUser());
	    String pXNAME = (String)getParameter(getRequest(),"XNAME");
	    String pFILE = (String)getParameter(getRequest(),"FILE");
	        
	    if (pXNAME == null && pFILE == null) {
	     	
	    	fail(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid Operation.");
	        	
	    } else if (pXNAME != null && pFILE == null) {
	        	
	       	if (this.isQueryVariableTrue("inbody")) {
	       		fail(Status.CLIENT_ERROR_BAD_REQUEST,"Please use HTTP PUT request to specify a file name in the URL.");
	       	} else  if (uploadUserFile(userPath,pXNAME)) {
	       		success(Status.SUCCESS_CREATED,"File(s) successfully uploaded");
	       	} else {
	       		fail(Status.SERVER_ERROR_INTERNAL,"Unable to complete upload.");
	       	}
	    } else if (pXNAME != null && pFILE != null) {
	    	fail(Status.CLIENT_ERROR_BAD_REQUEST,"Please use HTTP PUT request to specify a file name in the URL.");
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
	
	public void success(Status status, String msg){
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
	}
	
	@Override
	public void handlePut() {
		
		try {
			
			String userPath = org.nrg.xdat.security.helpers.Users.getUserCacheUploadsPath(getUser());
	        String pXNAME = (String)getParameter(getRequest(),"XNAME");
	        String pFILE = (String)getParameter(getRequest(),"FILE");
	        
	        if (pXNAME == null && pFILE == null) {
	        	
	        	fail(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid Operation.");
	        	
	        } else if (pXNAME != null && pFILE == null) {
	        	
	        	createUserResource(userPath,pXNAME);
	        	
	        } else if (pXNAME != null) {
//	        	commenting this out because we currently need this feature.
//	        	if (this.isQueryVariableTrue("extract")) {
//	        		// PUT Specification wants to enable a GET request on the same URL.  Wouldn't want to put extracted files without the 
//	        		// original archive file, or would't want to create and delete it.  Could possibly enable extraction by create, then 
//	        		// extracting the archive file without removing it.
//	        		fail(Status.CLIENT_ERROR_BAD_REQUEST,"File extraction not supported under HTTP PUT requests.");
//	        		return;
//	        	} 
	        	uploadUserFile(userPath,pXNAME,pFILE);
	        	
	        }
        
		} catch (Exception e) {
			fail(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			logger.error("",e);
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
	@SuppressWarnings("unchecked")
	private void returnFileList(String userPath,String pXNAME) {
		
		File dir = new File (userPath,pXNAME);
		
		if (dir.exists() && dir.isDirectory()) {
			
			ArrayList<File> fileList = new ArrayList<>();
			fileList.addAll(FileUtils.listFiles(dir,null,true));
			//Implement a sorting comparator on file list: Unnecessary, it is sorted by the representTable method.
	        ArrayList<String> columns= new ArrayList<>();
	        columns.add("Name");
	        columns.add("Size");
	        columns.add("URI");
	        
	        XFTTable table=new XFTTable();
	        table.initTable(columns);

			for (final File file : fileList) {
	        	String path=constructPath(file);
	        	Object[] oarray = new Object[] { path.substring(1), file.length(), constructURI(path) };
	        	table.insertRow(oarray);
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
				logger.error("",e);
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
			return handleInbodyUserFileUpload(userPath,dirString,fileName);
		} else {
			return handleAttachedUserFileUpload(userPath,dirString,fileName);
		}
		
	}
	
	
	private boolean handleInbodyUserFileUpload(String userPath, String dirString, String fileName) {
		
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
	private boolean handleAttachedUserFileUpload(String userPath, String dirString, String requestedName) {
		
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
        	if (Arrays.asList(XDAT.getSiteConfigPreferences().getZipExtensionsAsArray()).contains(file_extension)) {
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
	
	@SuppressWarnings("unchecked")
	private void returnZippedFiles(String userPath, String pXNAME, String pFILE) throws ActionException {
		ArrayList<File> fileList=new ArrayList<File>();
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

	@SuppressWarnings("unchecked")
	private void returnZippedFiles(String userPath, String pXNAME) throws ActionException {
		
		File dir = new File (userPath,pXNAME);
		if (dir.exists() && dir.isDirectory()) {
			ArrayList<File> fileList = new ArrayList<File>();
			fileList.addAll(FileUtils.listFiles(dir,null,true));
			sendZippedFiles(userPath,pXNAME,pXNAME,fileList);
		} else {
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"User directory not found or is not a directory.");
		}
		
	}
	
	private void sendZippedFiles(String userPath,String pXNAME,String fileName,ArrayList<File> fileList) throws ActionException {
		
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
	
}
