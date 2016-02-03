/*
 * org.nrg.xnat.restlet.services.RefreshCatalog
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/30/13 2:58 PM
 */

/**
 *
 */
package org.nrg.xnat.restlet.services;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.xft.event.EventUtils;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.nrg.xnat.utils.ResourceUtils;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

import java.util.Arrays;
import java.util.List;

public class RefreshCatalog extends SecureResource {

	public RefreshCatalog(Context context, Request request, Response response) {
		super(context, request, response);
	}

	@Override
	public boolean allowGet() {
		return true;
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	List<String> resources=Lists.newArrayList();
	ListMultimap<String,Object> otherParams=ArrayListMultimap.create();

    private boolean populateStats, checksum, delete, append = false;
	
	public void handleParam(final String key,final Object value) throws ClientException{
		if(value!=null){
			if(key.equals("resource")){
				resources.add((String)value);
			}else if(key.equals("populateStats")){
                populateStats=isQueryVariableTrueHelper(value);
			}else if(key.equals("checksum")){
				checksum=isQueryVariableTrueHelper(value);
			}else if(key.equals("delete")){
				delete=isQueryVariableTrueHelper(value);
			}else if(key.equals("append")){
				append=isQueryVariableTrueHelper(value);
			}else if(key.equals("options")){
				List<String> options=Arrays.asList(((String)value).split(","));
				if(options.contains("populateStats")){
					populateStats=true;
				}
				if(options.contains("checksum")){
					checksum=true;
				}
				if(options.contains("delete")){
					delete=true;
				}
				if(options.contains("append")){
					append=true;
				}
			}else{
				otherParams.put(key, value);
			}
		}
	}

	@Override
	public void handlePost() {
		try {
			final Representation entity = this.getRequest().getEntity();

			//parse body to identify resources if its multi-part form data
			//TODO: Handle JSON body.
			if (entity.isAvailable() && RequestUtil.isMultiPartFormData(entity)) {
				loadParams(new Form(entity));
			}
			loadQueryVariables();//parse query string to identify resources

			for(final String resource:resources){
				//parse passed URI parameter
				URIManager.DataURIA uri=UriParserUtils.parseURI(resource);

				if(!(uri instanceof ArchiveItemURI)) {
					throw new ClientException("Invalid Resource URI:"+ resource);
				}

                ArchiveItemURI resourceURI = (ArchiveItemURI) uri;

                ArchivableItem existenceCheck = resourceURI.getSecurityItem();
                if (existenceCheck != null) {
                    //call refresh operation
                    ResourceUtils.refreshResourceCatalog(resourceURI, user, this.newEventInstance(EventUtils.CATEGORY.DATA, "Catalog(s) Refreshed"), populateStats, checksum, delete, append);
                }
			}

			this.getResponse().setStatus(Status.SUCCESS_OK);
		} catch (ActionException e) {
			this.getResponse().setStatus(e.getStatus(), e.getMessage());
			logger.error("",e);
		} catch (Exception e) {
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
			logger.error("",e);
		}
	}


}
