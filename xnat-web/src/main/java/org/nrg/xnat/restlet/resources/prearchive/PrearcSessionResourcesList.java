/*
 * org.nrg.xnat.restlet.resources.prearchive.PrearcSessionResourcesList
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/3/14 9:54 AM
 */

/**
 * 
 */
package org.nrg.xnat.restlet.resources.prearchive;

import org.apache.log4j.Logger;
import org.nrg.action.ActionException;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatResourcecatalogI;
import org.nrg.xft.XFTTable;
import org.nrg.xnat.helpers.merge.MergeUtils;
import org.nrg.xnat.utils.CatalogUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * @author tolsen01
 *
 */
public class PrearcSessionResourcesList extends PrearcSessionResourceA {
	static Logger logger = Logger.getLogger(PrearcSessionResourcesList.class);
	
	public PrearcSessionResourcesList(Context context, Request request,
			Response response) {
		super(context, request, response);
	}


	
	final static ArrayList<String> columns=new ArrayList<String>(){
		private static final long serialVersionUID = 1L;
	{
		add("category");
		add("cat_id");
		add("label");
		add("file_count");
		add("file_size");
	}};

	@Override
	public Representation getRepresentation(Variant variant) {
		final MediaType mt=overrideVariant(variant);
				
		final PrearcInfo info;
		try {
			info = retrieveSessionBean();
		} catch (ActionException e) {
			setResponseStatus(e);
			return null;
		}
		
        final XFTTable table=new XFTTable();
        table.initTable(columns);
        for(final XnatImagescandataI scan: info.session.getScans_scan()){
	        for (final XnatAbstractresourceI res : scan.getFile()) {
	        	final String rootPath=CatalogUtils.getCatalogFile(info.session.getPrearchivepath(), ((XnatResourcecatalogI)res)).getParentFile().getAbsolutePath();
	        	CatalogUtils.Stats stats=CatalogUtils.getFileStats(CatalogUtils.getCleanCatalog(info.session.getPrearchivepath(), (XnatResourcecatalogI)res, false), rootPath);
	        	Object[] oarray = new Object[] { "scans", scan.getId(),res.getLabel(), stats.count, stats.size};
	        	table.insertRow(oarray);
	        }
        }
        
        return representTable(table, mt, new Hashtable<String,Object>());
	}
}
