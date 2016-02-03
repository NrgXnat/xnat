/*
 * org.nrg.xnat.restlet.resources.search.SearchFieldsVersionListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.resources.search;

import org.apache.log4j.Logger;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

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

	@Override
	public Representation getRepresentation(Variant variant) {	        
		try {
			SchemaElement se = SchemaElement.GetElement(elementName);
			ElementDisplay ed = se.getDisplay();
			
			MediaType mt = overrideVariant(variant);

			if (mt.equals(MediaType.APPLICATION_JSON)){
				return new StringRepresentation(ed.getVersionsJSON(),mt);
			}else{
				return new StringRepresentation(ed.getVersionsXML(),mt);
			}
		} catch (XFTInitException e) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
		} catch (ElementNotFoundException e) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
		}
	}
}
