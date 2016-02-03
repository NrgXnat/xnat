/*
 * org.nrg.xnat.restlet.resources.ScanTypeListing
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.resources;

import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.XFTTable;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.util.Hashtable;

public class ScanTypeListing  extends SecureResource {
	XnatProjectdata proj=null;
	
	public ScanTypeListing(Context context, Request request, Response response) {
		super(context, request, response);
		
		String pID = (String) getParameter(request,"PROJECT_ID");
		if (pID != null) {
			proj = XnatProjectdata.getProjectByIDorAlias(pID, user, false);

			if (proj == null) {
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			}
		}

		this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
		this.getVariants().add(new Variant(MediaType.TEXT_HTML));
		this.getVariants().add(new Variant(MediaType.TEXT_XML));	
	}

	@Override
	public Representation getRepresentation(Variant variant) {	
		XFTTable table = null;

		String scan_table = this.getQueryVariable("table");
		if (scan_table == null) {
			scan_table = "xnat_mrScanData";
		}else{
			if(!(scan_table.equalsIgnoreCase("xnat_mrScanData") || scan_table.equalsIgnoreCase("xnat_petScanData"))){
				AdminUtils.sendAdminEmail(user,"Possible SQL Injection attempt.", "User passed "+ scan_table+" as a table name.");
				this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
        		return null;
            }
		}
		
		try {
			String query = "SELECT xs_a_concat(series_description || ',') AS series_descriptions, scan.type FROM "
					+ scan_table
					+ " mr LEFT JOIN xnat_imageScanData scan ON mr.xnat_imageScanData_id=scan.xnat_imageScanData_id LEFT JOIN xnat_experimentData session ON scan.image_session_id=session.id";

			if (proj != null)
				query += " WHERE session.project='" + proj.getId() + "'";

			query += " GROUP BY scan.type ORDER BY scan.type";

			table = (XFTTable) XFTTable.Execute(query, user.getDBName(), user
					.getLogin());
		} catch (Exception e) {
			logger.error("",e);
		}
		
		Hashtable<String,Object> params=new Hashtable<String,Object>();
		params.put("title", "Scan Types");

		MediaType mt = overrideVariant(variant);

		if(table!=null)params.put("totalRecords", table.size());
		return this.representTable(table, mt, params);
	}
}