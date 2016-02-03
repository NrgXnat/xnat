/*
 * org.nrg.xnat.restlet.resources.ScannerListing
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
import org.nrg.xft.exception.DBPoolException;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.sql.SQLException;
import java.util.Hashtable;

public class ScannerListing  extends SecureResource {
	XnatProjectdata proj=null;
	
	public ScannerListing(Context context, Request request, Response response) {
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
			
		String scan_table=this.getQueryVariable("table");
		if(scan_table==null){
			scan_table="xnat_mrSessionData";
		}else{
			if(!(scan_table.equalsIgnoreCase("xnat_mrSessionData") || scan_table.equalsIgnoreCase("xnat_petSessionData") || scan_table.equalsIgnoreCase("xnat_ctSessionData"))){
				AdminUtils.sendAdminEmail(user,"Possible SQL Injection attempt.", "User passed "+ scan_table+" as a table name.");
				this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
        		return null;
            }
		}
		
		try {
			String query="SELECT DISTINCT isd.scanner FROM " + scan_table + " mod LEFT JOIN xnat_imageSessionData isd ON mod.id=isd.id LEFT JOIN xnat_experimentData expt ON isd.id=expt.id WHERE isd.scanner IS NOT NULL";

			if(proj!=null)query+=" WHERE expt.project='" + proj.getId() + "'";
			
			table=(XFTTable)XFTTable.Execute(query,user.getDBName(),user.getLogin());
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (DBPoolException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Hashtable<String,Object> params=new Hashtable<String,Object>();
		params.put("title", "Scanners");

		MediaType mt = overrideVariant(variant);

		if(table!=null)params.put("totalRecords", table.size());
		return this.representTable(table, mt, params);
	}
}