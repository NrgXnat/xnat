/*
 * org.nrg.xnat.restlet.services.AuditRestlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.services;

import com.google.common.collect.Maps;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.security.Authorizer;
import org.nrg.xft.ItemI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.utils.StringUtils;
import org.nrg.xnat.restlet.representations.ItemHTMLRepresentation;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import java.util.List;
import java.util.Map;

public class AuditRestlet extends SecureResource {
	ItemI item;
	final String key;
	final String xsiType;

	public AuditRestlet(Context context, Request request, Response response) {
		super(context, request, response);

		xsiType=this.filepath.substring(0, filepath.indexOf("/"));
		key=this.filepath.substring(filepath.indexOf("/")+1);
		

		List<String> ids=StringUtils.DelimitedStringToArrayList(key, ",");
		
		try {
			item=retrieveItemByIds(xsiType, ids);
		} catch (ActionException e) {
			respondToException(e,e.getStatus());
		} 

		if(item!=null){
	        this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	        this.getVariants().add(new Variant(MediaType.TEXT_HTML));
		}
	}
	
	private ItemI retrieve(final String xsiType, List<String> pks, List<String> ids) throws Exception{
		CriteriaCollection cc = new CriteriaCollection("AND");
		for(int i=0;i<pks.size();i++){
			cc.addClause(xsiType+"/"+pks.get(i), ids.get(i));
		}
		
		return ItemSearch.GetItems(xsiType, cc, this.user, false).getFirst();
	}
	
	public ItemI retrieveItemByIds(final String xsiType, List<String> ids) throws ActionException{		
		try {
			GenericWrapperElement element=GenericWrapperElement.GetElement(xsiType);
			
			
			List<String> pks=element.getPkNames();
			
			if(pks.size()!=ids.size()){
				throw new ClientException("Missing required primary key values");
			}
			
			ItemI i=retrieve(xsiType, pks, ids);
			
			if(i==null){
				i=retrieve(xsiType+"_history", pks, ids);
			}
			
			if(i!=null){
				Authorizer.getInstance().authorizeRead(i.getItem(), user);
			}
			
			return i;
		} catch (ElementNotFoundException e) {
			throw new ClientException(e);
		} catch (Exception e) {
			throw new ServerException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}

	
	@Override
	public Representation represent(Variant variant) throws ResourceException {
		MediaType mt=overrideVariant(variant);
		
		try {
			if(mt.equals(MediaType.TEXT_HTML)){
				String screen=getQueryVariable("requested_screen");
				if(screen==null){
					screen="WorkflowHistorySummary";
				}
				
				Map<String,Object> params=Maps.newHashMap();
				
				params.put("key", key);
				
				if(hasQueryVariable("includeFiles")){
					params.put("includeFiles", getQueryVariable("includeFiles"));
				}
				
				if(xsiType!=null){
					params.put("xsiType", xsiType);
				}
				
				if(key!=null){
					params.put("key", key);
				}
				
				if(hasQueryVariable("includeDetails")){
					params.put("includeDetails", getQueryVariable("includeDetails"));
				}
				
				params.put("hideTopBar",isQueryVariableTrue("hideTopBar"));
				
				return new ItemHTMLRepresentation(item.getItem(), MediaType.TEXT_HTML, getRequest(), user,screen,params);
			}else{
				return buildChangesets(item.getItem(), key, mt);
			}
		} catch (Exception e) {
			logger.error("",e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			return null;
		}
	}

	
}
