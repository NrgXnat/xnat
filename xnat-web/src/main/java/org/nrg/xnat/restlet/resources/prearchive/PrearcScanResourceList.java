/*
 * web: org.nrg.xnat.restlet.resources.prearchive.PrearcScanResourceList
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
import org.nrg.action.ActionException;
import org.nrg.action.ServerException;
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
@Slf4j
public class PrearcScanResourceList extends PrearcSessionResourceA {
	private static final String SCAN_ID = "SCAN_ID";

	protected final String scan_id;
	
	public PrearcScanResourceList(Context context, Request request,
			Response response) {
		super(context, request, response);
		scan_id = (String)getParameter(request,SCAN_ID);
	}


	
	final static ArrayList<String> columns=new ArrayList<String>(){
		private static final long serialVersionUID = 1L;
	{
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
		
		final XnatImagescandataI scan=MergeUtils.getMatchingScanById(scan_id,(List<XnatImagescandataI>)info.session.getScans_scan());
		
		if(scan==null){
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
		
        final XFTTable table=new XFTTable();
        table.initTable(columns);
        String project = scan.getProject();
        String prearchivePath = info.session.getPrearchivepath();
        for (final XnatAbstractresourceI res : scan.getFile()) {
			try {
				final CatalogUtils.CatalogData catalogData = CatalogUtils.CatalogData.getOrCreateAndClean(prearchivePath,
						(XnatResourcecatalogI) res, false, project);
				CatalogUtils.Stats stats = CatalogUtils.getFileStats(catalogData.catBean, catalogData.catPath,
						catalogData.project);
				table.insertRow(new Object[] { res.getLabel(), stats.count, stats.size});
			} catch (ServerException e) {
				log.error("Unable to read catalog for resource {}", res.getXnatAbstractresourceId(), e);
			}
        }
        
        return representTable(table, mt, new Hashtable<String,Object>());
	}
}
