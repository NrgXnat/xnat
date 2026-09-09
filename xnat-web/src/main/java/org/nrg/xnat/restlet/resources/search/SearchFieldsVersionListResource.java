/*
 * web: org.nrg.xnat.restlet.resources.search.SearchFieldsVersionListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.collections.DisplayFieldRefCollection;
import org.nrg.xdat.display.DisplayVersion;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.display.transport.entities.DisplayVersionDB;
import org.nrg.xdat.display.transport.services.ElementDisplayStorageService;
import org.nrg.xdat.display.transport.services.impl.HibernateElementDisplayService;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xnat.restlet.representations.JSONObjectRepresentation;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.*;
import org.restlet.routing.*;
import org.restlet.representation.*;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;

import java.io.*;

/**
 * @author timo
 *
 */
public class SearchFieldsVersionListResource extends SecureResource {
	static org.apache.log4j.Logger logger = Logger.getLogger(SearchFieldsVersionListResource.class);
	private String elementName=null;
	public SearchFieldsVersionListResource(Context context, Request request, Response response) {
		super(context, request, response);
		
		elementName = (String) getParameter(request,"ELEMENT_NAME");
		if (elementName != null) {
			this.getVariants().add(new Variant(MediaType.TEXT_XML));
		} else {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
			
	}


	private ElementDisplayStorageService _elementDisplayService;

	private ElementDisplayStorageService getElementDisplayService() {
		if (_elementDisplayService == null) {
			_elementDisplayService = XDAT.getContextService().getBean(ElementDisplayStorageService.class);
		}
		return _elementDisplayService;
	}

	@Override
	public boolean allowPut() {
		return true;
	}

	@Override
	public void handlePut() {
		if(Roles.isSiteAdmin(this.getUser())) {
			if (filepath != null && !filepath.equals("")) {
				if (filepath.startsWith("modify")) {
					final Representation entity = getRequest().getEntity();
					if (RequestUtil.compareMediaType(entity, MediaType.APPLICATION_JSON)) {
						try {
							ObjectMapper mapper = new ObjectMapper();
							DisplayVersionDB dv = mapper.readValue(entity.getStream(), DisplayVersionDB.class);

							this.getElementDisplayService().addOrUpdateDisplayVersion(elementName, dv);
						} catch (IOException e) {
							logger.error("", e);
						} catch (HibernateElementDisplayService.NoFieldFound e) {
							logger.error("", e);
						} catch (DisplayFieldRefCollection.DisplayFieldRefNotFoundException e) {
							logger.error("", e);
						} catch (HibernateElementDisplayService.NoVersionFound e) {
							logger.error("", e);
						} catch (HibernateElementDisplayService.NoSuchElementException e) {
							logger.error("", e);
						}
					} else {
						getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
						return;
					}
				}
			}
		}else{
			this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			return;
		}
	}

	@Override
	public Representation getRepresentation(Variant variant) {	        
		try {
			MediaType mt = overrideVariant(variant);

			if(this.isQueryVariableTrue("listViews")){
				//list the versions configured from the in-memory ED
				SchemaElement se = SchemaElement.GetElement(elementName);
				ElementDisplay ed = se.getDisplay();
				JSONObject obj = new JSONObject();
				JSONArray array = new JSONArray();
				for(DisplayVersion dv : ed.getVersions().values()){
					array.put(dv.getVersionName());
				}
				obj.put("views",array);

				return new JSONObjectRepresentation(mt, obj);
			}else if (this.isQueryVariableTrue("v2") && StringUtils.isNotBlank(filepath)) {
				SchemaElement se = SchemaElement.GetElement(elementName);
				ElementDisplay ed = se.getDisplay();

				DisplayVersion dv = ed.getVersion(filepath);
				if(dv==null){
					getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return null;
				}

				DisplayVersionDB storedDV = getElementDisplayService().convertDisplayVersion(dv);

				return new JSONJacksonRepresentation(MediaType.APPLICATION_JSON,storedDV);
			}else{
				SchemaElement se = SchemaElement.GetElement(elementName);
				ElementDisplay ed = se.getDisplay();


				if (mt.equals(MediaType.APPLICATION_JSON)){
					return new StringRepresentation(ed.getVersionsJSON(),mt);
				}else{
					return new StringRepresentation(ed.getVersionsXML(),mt);
				}
			}
		} catch (XFTInitException e) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
		} catch (ElementNotFoundException e) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
		}
	}

	public class JSONJacksonRepresentation extends OutputRepresentation {
		Object o;

		public JSONJacksonRepresentation(MediaType mediaType, Object o) {
			super(mediaType);
			this.o = o;
		}

		public void write(OutputStream os) throws IOException {
			try {
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));

				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(writer,o);
			} catch (JSONException var3) {
				new IOException(var3);
			}

		}
	}
}
