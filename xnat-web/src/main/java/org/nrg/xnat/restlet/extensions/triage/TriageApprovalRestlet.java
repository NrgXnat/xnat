//Copyright 2012 Radiologics, Inc
//Author: James Dickson <james@radiologics.com>
package org.nrg.xnat.restlet.extensions.triage;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Features;
import org.nrg.xft.exception.InvalidItemException;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.DataURIA;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.restlet.XnatRestlet;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.nrg.xnat.services.triage.TriageService;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;

@XnatRestlet(value={"/services/triage/approve"})
public class TriageApprovalRestlet extends SecureResource {
	private static final String _ON_SUCCESS_RETURN_JS = "_onSuccessReturnJS";
	private static final String _ON_SUCCESS_RETURN_HTML = "_onSuccessReturnHTML";
	private static final String OVERWRITE = "overwrite";
	private static final String SRC = "src";
	private static final String DEST = "dest";
	
	private final static Logger logger = LoggerFactory.getLogger(TriageApprovalRestlet.class);
	
	
	private TriageService triageService;


	public TriageApprovalRestlet(Context context, Request request, Response response) {
		super(context, request, response);
	}
	
	
	
	@Override
	public boolean allowPost() {
		return true;
	}
	@Override
	public boolean allowPut() {
		return true;
	}
	Map<URIManager.DataURIA,ResourceURII> moves=Maps.newHashMap();
	Boolean overwrite=Boolean.FALSE;
		
	String src=null,dest=null;
	
	Date eventTime=Calendar.getInstance().getTime();
	
	ListMultimap<String,Object> otherParams=ArrayListMultimap.create();

	public void handleParam(final String key,final Object value) throws ClientException{
		if(value!=null){
			if(key.contains("/")){
				moves.put(convertKey(key), convertValue((String)value));
			}else if(key.equals(SRC)){
				src=(String)value;
			}else if(key.equals(DEST)){
				dest=(String)value;
			}else if(key.equals(OVERWRITE)){
				overwrite=Boolean.valueOf((String)value);
			}else{
				otherParams.put(key, value);
			}
		}
	}
	
	public URIManager.DataURIA convertKey(final String key) throws ClientException{
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

	@Override
	public void handlePut() {	
		this.handlePost();
	}
	@Override
	public void handlePost() {		
		//build fileWriters
		try {
			List<String> duplicates=new ArrayList<String>();
			final Representation entity = this.getRequest().getEntity();
													
			if (RequestUtil.isMultiPartFormData(entity)) {
				loadParams(new Form(entity));
			}
			
			loadQueryVariables();		
			
			if(StringUtils.isNotEmpty(src)){
				if(StringUtils.isEmpty(dest)){
					this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Missing dest path");
					return;
				}
				DataURIA uriSource=convertKey(src);
				ResourceURII uriDestination=convertValue(dest);
				
	        	
				if(!canEditSource(uriSource)){
					this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Unauthorized");
					return;
				}
				
				if(!canEditDestination(uriDestination)){
					this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Unauthorized");
					return;
				}
				moves.put(uriSource, uriDestination);
			}
			
			if(moves.size()==0){
				this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Missing src and dest path");
				return;
			}
			
			
			for(Entry<URIManager.DataURIA, ResourceURII> entry: moves.entrySet()){
				boolean locked=this.getTriageService().isLocked(this.getUser(), this.getEventId(), overwrite, otherParams, entry.getKey(), entry.getValue());
				if(!locked){
					duplicates.addAll(this.getTriageService().move(this.getUser(),this.getEventId(),overwrite,otherParams,entry.getKey(),entry.getValue()));
					if(!overwrite && duplicates.size()>0){
						success(Status.SUCCESS_CREATED,"Duplicate File(s) found.");
					}else{
						success(Status.SUCCESS_CREATED,"Quarantine File(s) successfully approved");
					}
				}else{
					success(Status.SUCCESS_OK,"Destination is locked. Please unlock to continue.");
				}
			}
			
		} catch (ActionException e) {
			this.getResponse().setStatus(e.getStatus(), e.getMessage());
			logger.error("",e);
			return;
		} catch (Exception e) {
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
			logger.error("",e);
			return;
		}
	}
	
	
	private TriageService getTriageService() {
        if (triageService == null) {
            triageService = XDAT.getContextService().getBean(TriageService.class);
        }
        return triageService;
    }
	

	
	public void success(Status status, String msg){
		this.getResponse().setStatus(status,msg);
		
		String _return =this.retrieveParam(_ON_SUCCESS_RETURN_JS);
		if(_return !=null){
			getResponse().setEntity(new StringRepresentation("<script>"+_return + "</script>", MediaType.TEXT_HTML));
		}else{
			_return =this.retrieveParam(_ON_SUCCESS_RETURN_HTML);
			if(_return !=null){
				getResponse().setEntity(new StringRepresentation(msg , MediaType.TEXT_HTML));
			}else{
				getResponse().setEntity(new StringRepresentation(msg , MediaType.TEXT_HTML));
			}
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
	
	private boolean canEditSource(final DataURIA uriSource) throws InvalidItemException, Exception{
		boolean authorized=false;
		try{
			
			String sproject=(String)uriSource.getProps().get(URIManager.PROJECT_ID);
			XnatProjectdata proj = XnatProjectdata.getProjectByIDorAlias(sproject, this.getUser(), false);
			if(proj!=null && Features.checkFeature(this.getUser(),proj.getSecurityTags().getHash().values(), "QuarantineReview")){
				authorized=true;
			}else{
				authorized=false;
			}
		}catch(Exception e){
			authorized=false;
		}
		return authorized;
	}

	
	
	private boolean canEditDestination(final ResourceURII arcURI) throws InvalidItemException, Exception{
		boolean authorized=false;
		if(arcURI.getSecurityItem().canEdit(this.getUser())){
			authorized=true;
			//throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, new Exception("Unauthorized"));
		}else{
			authorized=false;
		}
		return authorized;
	}
	
	
	
	
	

	public void setTriageService(TriageService triageService) {
		this.triageService = triageService;
	}
}