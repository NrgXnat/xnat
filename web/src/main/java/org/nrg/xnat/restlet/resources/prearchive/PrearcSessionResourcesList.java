/*
 * web: org.nrg.xnat.restlet.resources.prearchive.PrearcSessionResourcesList
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * 
 */
package org.nrg.xnat.restlet.resources.prearchive;

import lombok.extern.slf4j.Slf4j;
import org.apache.ecs.xhtml.table;
import org.nrg.action.ActionException;
import org.nrg.action.ServerException;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatResourceI;
import org.nrg.xdat.model.XnatResourcecatalogI;
import org.nrg.xft.XFTTable;
import org.nrg.xnat.utils.CatalogUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * @author tolsen01
 *
 */
@Slf4j
public class PrearcSessionResourcesList extends PrearcSessionResourceA {

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
		String project = info.session.getProject();
		String prearchivePath = info.session.getPrearchivepath();
		for (final XnatImagescandataI scan: info.session.getScans_scan()){
			for (final XnatAbstractresourceI res : scan.getFile()) {
				addRow(res, table, "scans", scan.getId(), prearchivePath, project);
			}
		}
		for (final XnatAbstractresourceI res : info.session.getResources_resource()) {
			addRow(res, table, "resources", res.getXnatAbstractresourceId(), prearchivePath, project);
		}
		
		return representTable(table, mt, new Hashtable<String,Object>());
	}

	private void addRow(XnatAbstractresourceI res, XFTTable table, String category, Object categoryId,
						String prearchivePath, String project) {
		if (res instanceof XnatResourcecatalogI) {
			try {
				final CatalogUtils.CatalogData catalogData = CatalogUtils.CatalogData.getOrCreateAndClean(prearchivePath,
						(XnatResourcecatalogI) res, false, project);
				CatalogUtils.Stats stats = CatalogUtils.getFileStats(catalogData.catBean, catalogData.catPath,
						catalogData.project);
				table.insertRow(new Object[]{category, categoryId, res.getLabel(), stats.count, stats.size});
			} catch (ServerException e) {
				log.error("Unable to read catalog for resource {}", res.getXnatAbstractresourceId(), e);
			}
		} else if (res instanceof XnatResourceI) {
			File f = new File(prearchivePath, ((XnatResourceI) res).getUri());
			if (f.exists()) {
				Object[] oarray = new Object[]{category, categoryId, res.getLabel(), 1, f.length()};
				table.insertRow(oarray);
			} else {
				Object[] oarray = new Object[]{category, categoryId, res.getLabel(), 0, 0};
				table.insertRow(oarray);
			}
		}
	}
}
