/*
 * web: org.nrg.xnat.restlet.services.Importer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.services;

import com.google.common.collect.Lists;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.framework.utilities.Reflection;
import org.nrg.status.StatusList;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.file.StoredFile;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.transactions.HTTPSessionStatusManagerQueue;
import org.nrg.xnat.helpers.transactions.PersistentStatusQueueManagerI;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.helpers.uri.UriParserUtils.UriParser;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerPackages;
import org.nrg.xnat.restlet.actions.importer.ImporterNotFoundException;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.restlet.util.XNATRestConstants;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.restlet.util.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class Importer extends SecureResource {
	private static final String CRLF = "\r\n";
	private static final String HTTP_SESSION_LISTENER = "http-session-listener";
	private final Logger logger = LoggerFactory.getLogger(Importer.class);

	public Importer(Context context, Request request, Response response) {
		super(context, request, response);

		this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
		this.getVariants().add(new Variant(MediaType.TEXT_HTML));
		this.getVariants().add(new Variant(MediaType.TEXT_XML));
		this.getVariants().add(new Variant(MediaType.TEXT_PLAIN));
	}
	
	private static final List<String> HANDLERS_ALLOWING_CALLS_WITHOUT_FILES = Lists.newArrayList(); 
	private static final List<String> HANDLERS_PREFERRING_PARTIAL_URI_WRAP = Lists.newArrayList(); 
	static {
        final ImporterHandlerPackages packages = XDAT.getContextService().getBean("importerHandlerPackages",ImporterHandlerPackages.class);
        for (final String pkg : packages) {
			try {
				final List<Class<?>> classesForPackage = Reflection.getClassesForPackage(pkg);
				for (final Class<?> clazz : classesForPackage) {
                    if (ImporterHandlerA.class.isAssignableFrom(clazz)) {
                        if (!clazz.isAnnotationPresent(ImporterHandler.class)) {
                       		continue;
                       	}
                       	ImporterHandler anno = clazz.getAnnotation(ImporterHandler.class);
                        if (anno!=null && anno.allowCallsWithoutFiles()) {
                        	HANDLERS_ALLOWING_CALLS_WITHOUT_FILES.add(anno.handler());
                        }
                        if (anno!=null && anno.callPartialUriWrap()) {
                        	HANDLERS_PREFERRING_PARTIAL_URI_WRAP.add(anno.handler());
                        }
                    }
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
        }
	}

	@Override
	public boolean allowGet(){
		return false;
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	List<FileWriterWrapperI> fw= new ArrayList<>();

	String handler=null;
	String listenerControl=null;
	boolean httpSessionListener=false;

	final Map<String,Object> params= new Hashtable<>();

	List<String> response=null;

	@Override
	public void handleParam(String key, Object value) throws ClientException {
		switch (key) {
			case ImporterHandlerA.IMPORT_HANDLER_ATTR:
				handler = (String) value;
				break;
			case XNATRestConstants.TRANSACTION_RECORD_ID:
				listenerControl = (String) value;
				break;
			case "src":
				fw.add(retrievePrestoreFile((String) value));
				break;
			case HTTP_SESSION_LISTENER:
				listenerControl = (String) value;
				httpSessionListener = true;
				break;
			default:
				params.put(key, value);
				break;
		}
	}
	
	@Override
	public void handlePost() {
		//build fileWriters
		final UserI user = getUser();
		try {
		    final Request request = getRequest();
		    if (logger.isDebugEnabled()) {
		        final ClientInfo client = request.getClientInfo();
				logger.debug("handling POST from " + client.getAddress() + ":" + client.getPort() + " " + client.getAgent());
		    }
		    
			Representation entity = request.getEntity();

			fw=this.getFileWriters();

			//maintain parameters
			loadQueryVariables();
			
			ImporterHandlerA importer;
			
			// Set the overwrite flag if we are uploading directly to the archive (prearchive_code = 1)
			String prearchive_code = (String)params.get("prearchive_code");
			if("1".equals(prearchive_code)){ // User has selected archive option
				
				// If the overwrite flag has been set by the user, make sure it is a valid option
				if(params.containsKey("overwrite")){
					String ow = (String)params.get("overwrite");
					if(!PrearcUtils.DELETE.equalsIgnoreCase(ow) || !PrearcUtils.APPEND.equalsIgnoreCase(ow)){
						this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Overwrite flag was not set to a valid option. ('append' or 'delete')");
						return;
					}
				// If the overwrite flag has not been set by the user, set the flag based on
				// the project setting.
				}else{
					// Get the prearchive code for the project specified. 
					XnatProjectdata proj = XnatProjectdata.getProjectByIDorAlias((String)params.get("project"), user, true);
					PrearchiveCode pCode = PrearchiveCode.code(proj.getArcSpecification().getPrearchiveCode());
				
					// If the project is set to auto archive overwrite
					if(pCode ==  PrearchiveCode.AutoArchiveOverwrite){
						params.put("overwrite",PrearcUtils.DELETE);
					}
					else{ // If the project is set to append or prearchive-only. 
						params.put("overwrite",PrearcUtils.APPEND);
					}
				}
			}
			
			if(fw.size()==0 && handler != null && !HANDLERS_ALLOWING_CALLS_WITHOUT_FILES.contains(handler)) {
				
				this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unable to identify upload format.");
				return;
				
			} else if (handler != null && fw.size() == 0) {
				
				try {				
					importer = ImporterHandlerA.buildImporter(handler, 
															  listenerControl, 
															  user, 
															  null, // FileWriterWrapperI is null because no files should have been uploaded. 
															  params);
				}
				catch (Exception e) {
					logger.error("",e);
					throw new ServerException(e.getMessage(),e);
				}

				if (storeStatusList(importer)) {
					return;
				}

				response= importer.call();
							
				if(entity!=null && APPLICATION_XMIRC.equals(entity.getMediaType())){
					returnString("OK", Status.SUCCESS_OK);
					return;
				}
				
				returnDefaultRepresentation();
				return;
				
			} else if (fw.size()>1){
				
				this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Importer is limited to one uploaded resource at a time.");
				return;
				
			}

			if(handler==null && entity!=null){
				if(APPLICATION_DICOM.equals(entity.getMediaType()) || 
						APPLICATION_XMIRC.equals(entity.getMediaType()) || 
						APPLICATION_XMIRC_DICOM.equals(entity.getMediaType())){
					handler=ImporterHandlerA.GRADUAL_DICOM_IMPORTER;
				}
			}
			
			try {
				importer = ImporterHandlerA.buildImporter(handler, listenerControl, user, fw.get(0), params);
			} catch (SecurityException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
				logger.error("",e);
				throw new ServerException(e.getMessage(),e);
			} catch (IllegalArgumentException | NoSuchMethodException e) {
				logger.error("",e);
				throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,e.getMessage(),e);
			} catch (ImporterNotFoundException e) {
				logger.error("",e);
				throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND,e.getMessage(),e);
				}

			if (storeStatusList(importer)) {
				return;
			}

			response= importer.call();
						
			if(entity!=null && APPLICATION_XMIRC.equals(entity.getMediaType())){
				returnString("OK", Status.SUCCESS_OK);
				return;
			}
			
			returnDefaultRepresentation();
		} catch (ClientException e) {
			respondToException(e,(e.status!=null)?e.status:Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (ServerException e) {
			respondToException(e,(e.status!=null)?e.status:Status.SERVER_ERROR_INTERNAL);
		}catch (IllegalArgumentException | FileUploadException e) {
			respondToException(e,Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	public boolean storeStatusList(final ImporterHandlerA importer) {
		if(httpSessionListener){
            if(StringUtils.isEmpty(listenerControl)){
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "'" + XNATRestConstants.TRANSACTION_RECORD_ID + "' is required when requesting '" + HTTP_SESSION_LISTENER + "'.");
				return true;
            }
            final StatusList sq = new StatusList();
            importer.addStatusListener(sq);

            storeStatusList(listenerControl, sq);
        }
		return false;
	}

	protected void respondToException(Exception e, Status status) {
    	final Throwable cause = e.getCause();
		if (cause != null && cause instanceof ExceptionInInitializerError && ((ExceptionInInitializerError) cause).getException() != null) {
			final ExceptionInInitializerError error = (ExceptionInInitializerError) cause;
			final StringBuilder buffer = new StringBuilder("An error occurred initializing an object during the import operation: ");
			buffer.append(error.getException().getMessage());
			final StackTraceElement[] stackTrace = error.getException().getStackTrace();
			if (stackTrace != null) {
				int lines = 0;
				for (final StackTraceElement element : stackTrace) {
					buffer.append(System.lineSeparator()).append("    ").append(element.toString());
					lines++;
					if (lines > 5) {
						break;
					}
				}
			}
			logger.error(buffer.toString());
		} else {
			logger.error("", e);
		}
		if (this.requested_format!=null && this.requested_format.equalsIgnoreCase("HTML")) {
			response = new ArrayList<>();
			response.add(e.getMessage());
			returnDefaultRepresentation();
		} else {
			this.getResponse().setStatus(status, e.getMessage());
		}
	}

	@Override
		public Representation represent(Variant variant) throws ResourceException {
			final MediaType mt=overrideVariant(variant);
			final boolean wrapURI = (handler==null || HANDLERS_PREFERRING_PARTIAL_URI_WRAP.contains(handler));
			if(mt.equals(MediaType.TEXT_HTML)){
				return buildHtmlResponse(response);
			}else if(mt.equals(MediaType.TEXT_PLAIN)){
				if(response!=null && response.size()==1){
					return new StringRepresentation((wrapURI) ? wrapPartialDataURI(response.get(0)) : response.get(0), MediaType.TEXT_PLAIN);
				}else{
					return new StringRepresentation(convertListToString(response,wrapURI), MediaType.TEXT_PLAIN);
				}
			}else{
				return new StringRepresentation(convertListToString(response, wrapURI), MediaType.TEXT_URI_LIST);
			}
		}

		private Representation buildHtmlResponse(List<String> response) {
			final List<String> preList= new ArrayList<>();
			final List<String> archList= new ArrayList<>();
			final StringBuilder sb=new StringBuilder("<html><head>");
			sb.append("<link type='text/css' rel='stylesheet' href='");
			sb.append(TurbineUtils.GetRelativePath(this.getHttpServletRequest()));
			sb.append("/style/xdat.css'>");
			sb.append("<link type='text/css' rel='stylesheet' href='/xnat/style/xnat.css'>");
			sb.append("</head><body class='yui-skin-sam'>");
			
			for(final String s: response){
				final URIManager.DataURIA obj;
				try {
					obj = UriParserUtils.parseURI(s);
					if(obj instanceof URIManager.ArchiveURI){
						//is an archive session
						archList.add(s);
					}else{
						//is a prearchive session
						preList.add(s);
					}
				} catch (MalformedURLException e) {
					// Do nothing, return empty text
				}
			}
			if (!(preList.isEmpty() && archList.isEmpty())) {
				sb.append("The following sessions have been uploaded:<br>");
				if (!preList.isEmpty()) {
					try {
							sb.append("<br>&nbsp;&nbsp;&nbsp;<a target='_parent' href='");
							sb.append(TurbineUtils.GetRelativePath(this.getHttpServletRequest()));
							sb.append("/app/template/XDATScreen_prearchives.vm'>");
							sb.append(preList.size());
							sb.append(" sessions(s)</a> has been moved to the pre-archive");
					} catch (Exception e) {
						sb.append("<br>A total of ");
						sb.append(preList.size());
						sb.append(" session(s) have been moved to pre-archive.<br>");
					}
				}
				if (!archList.isEmpty()) {
					try {
						for (final String s : archList) {
							final String[] sarray=s.split("/");
							sb.append("<br>&nbsp;&nbsp;&nbsp;<a target='_parent' href='");
							sb.append(TurbineUtils.GetRelativePath(this.getHttpServletRequest()));
							sb.append("/data");
							sb.append(s);
							sb.append("'>");
							sb.append(sarray[7]);
							sb.append("</a> has been archived for project ").append(sarray[3]);
						}
					} catch (Exception e) {
						sb.append("<br>A total of ");
						sb.append(archList.size());
						sb.append(" session(s) have archived.<br>");
					}
				}
			sb.append("<script type='text/javascript'>");
			sb.append("if(window.parent.proceed!=undefined){window.parent.proceed();}");
			sb.append("</script>");
				sb.append("</body></html>");
			} else {
				sb.append("ERROR: The process could not be completed due to exceptions - <br>");
				for (final String s : response) {
					sb.append("<br><span style='color:#DD0000'>");
					sb.append(s);
					sb.append("</span>");
				}
				sb.append("Your data may be available in the prearchive for your review.</body><script type='text/javascript'>");
				sb.append("parent.document.getElementById('ex').style.display='none';");
				sb.append("parent.toggleExtractSummary();");
				sb.append("</script></html>");
			}
		return new StringRepresentation(sb.toString(),MediaType.TEXT_HTML);
	}

	public FileWriterWrapperI retrievePrestoreFile(final String src) throws ClientException{

		Map<String,Object> map=new UriParser("/user/cache/resources/{XNAME}/files/{FILE}",Template.MODE_EQUALS).readUri(src);

		if(!map.containsKey("XNAME") || !map.containsKey("FILE")){
			throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,"src uri is invalid.",new Exception());
		}

		File f=org.nrg.xdat.security.helpers.Users.getUserCacheFile(getUser(), (String)map.get("XNAME"), (String)map.get("FILE"));

		if(f.exists()){
			return new StoredFile(f,true);
		}else{
			throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND,"Unknown src file.",new Exception());
		}
	}

	private String convertListToString(final List<String> response, boolean wrapPartialDataURI){
		final StringBuilder sb = new StringBuilder();
		for(final String s:response){
			sb.append((wrapPartialDataURI) ? wrapPartialDataURI(s) : s).append(CRLF);
		}

		return sb.toString();
	}

	private void storeStatusList(final String transaction_id,final StatusList sl) throws IllegalArgumentException{
		this.retrieveSQManager().storeStatusQueue(transaction_id, sl);
	}

	private PersistentStatusQueueManagerI retrieveSQManager(){
		return new HTTPSessionStatusManagerQueue(this.getHttpSession());
	}


}
