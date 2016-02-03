/*
 * org.nrg.xnat.restlet.resources.InvestigatorListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.resources;

import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.sql.SQLException;
import java.util.Hashtable;

public class InvestigatorListResource extends SecureResource {
	XFTTable table = null;

	public InvestigatorListResource(Context context, Request request, Response response) {
		super(context, request, response);
		
			this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
			this.getVariants().add(new Variant(MediaType.TEXT_HTML));
			this.getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	@Override
	public boolean allowGet() {
		return true;
	}

	@Override
	public Representation getRepresentation(Variant variant) {	
		Hashtable<String,Object> params=new Hashtable<String,Object>();
		params.put("title", "Investigators");

		MediaType mt = overrideVariant(variant);
		
		try {
			String query = "SELECT DISTINCT ON ( inv.lastname,inv.firstname) inv.firstname,inv.lastname,inv.institution,inv.department,inv.email,inv.xnat_investigatorData_id,login FROM xnat_investigatorData inv LEFT JOIN xdat_user u ON ((lower(inv.firstname)=lower(u.firstname) AND lower(inv.lastname)=lower(u.lastname)) OR inv.email=u.email) ORDER BY inv.lastname,inv.firstname";
			table = XFTTable.Execute(query, user.getDBName(), user.getLogin());
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (DBPoolException e) {
			e.printStackTrace();
		}

		if(table!=null)params.put("totalRecords", table.size());
		return this.representTable(table, mt, params);
	}
}