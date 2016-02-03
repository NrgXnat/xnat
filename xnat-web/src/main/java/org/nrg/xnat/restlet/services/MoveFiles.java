/*
 * org.nrg.xnat.restlet.services.MoveFiles
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.services;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xnat.helpers.move.FileMover;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class MoveFiles extends SecureResource {

	
	private static final String OVERWRITE = "overwrite";
	private static final String SRC = "src";
	private static final String DEST = "dest";
	
	private final static Logger logger = LoggerFactory.getLogger(MoveFiles.class);
	
	public MoveFiles(Context context, Request request, Response response) {
		super(context, request, response);
				
	}
	
	@Override
	public boolean allowPost() {
		return true;
	}

	Map<URIManager.UserCacheURI,ResourceURII> moves=Maps.newHashMap();
	Boolean overwrite=null;
		
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
	
	public URIManager.UserCacheURI convertKey(final String key) throws ClientException{
		try {
			URIManager.DataURIA uri=UriParserUtils.parseURI(key);
			
			if(uri instanceof URIManager.UserCacheURI){
				return (URIManager.UserCacheURI)uri;
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
	public void handlePost() {		
		//build fileWriters
		try {
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

				moves.put(convertKey(src), convertValue(dest));
			}
			
			if(moves.size()==0){
				this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Missing src and dest path");
				return;
			}
			

			EventMetaI ci;
			PersistentWorkflowI work=PersistentWorkflowUtils.getWorkflowByEventId(user, getEventId());
			if(work!=null){
				ci=work.buildEvent();
			}else{
				ci = EventUtils.DEFAULT_EVENT(user,null);
			}
			
			//this should allow injection of a different implementation- TO
			final FileMover mover =new FileMover(overwrite,user,otherParams);
			
			for(Map.Entry<URIManager.UserCacheURI,ResourceURII> entry: moves.entrySet()){
				mover.call(entry.getKey(),entry.getValue(),ci);
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
}
