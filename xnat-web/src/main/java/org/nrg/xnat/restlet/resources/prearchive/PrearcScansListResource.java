/*
 * org.nrg.xnat.restlet.resources.prearchive.PrearcScansListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.resources.prearchive;

import org.apache.log4j.Logger;
import org.nrg.action.ActionException;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xft.XFTTable;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.util.ArrayList;
import java.util.Hashtable;

public class PrearcScansListResource extends PrearcSessionResourceA {
	static Logger logger = Logger.getLogger(PrearcScansListResource.class);
	
	public PrearcScansListResource(Context context, Request request,
			Response response) {
		super(context, request, response);
	}
	
	final static ArrayList<String> columns=new ArrayList<String>(){
		private static final long serialVersionUID = 1L;
	{
		add("ID");
		add("xsiType");
		add("series_description");
	}};

	@Override
	public Representation getRepresentation(Variant variant) {
		MediaType mt=overrideVariant(variant);
				
		
		PrearcInfo info;
		try {
			info = retrieveSessionBean();
		} catch (ActionException e) {
			setResponseStatus(e);
			return null;
		}
		
        XFTTable table=new XFTTable();
        table.initTable(columns);
        for (XnatImagescandataI scan : info.session.getScans_scan()) {
        	Object[] oarray = new Object[] { scan.getId(), scan.getXSIType(), scan.getSeriesDescription()};
        	table.insertRow(oarray);
        }
        
        return representTable(table, mt, new Hashtable<String,Object>());
	}
	
	

}
