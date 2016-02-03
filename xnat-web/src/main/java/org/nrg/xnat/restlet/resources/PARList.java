/*
 * org.nrg.xnat.restlet.resources.PARList
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.resources;

import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.XFTTable;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.util.Hashtable;

/**
 * @author timo
 * 
 */
public class PARList extends SecureResource {
	public PARList(Context context, Request request, Response response) {
		super(context, request, response);
		getVariants().addAll(STANDARD_VARIANTS);
        if (!Roles.isSiteAdmin(user)) {
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Only administrators can access the list of project access requests.");
        }
	}

	@Override
	public Representation represent(Variant variant) {
		XFTTable table = new XFTTable();
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		try {
			table = XFTTable
					.Execute(
							"SELECT par.par_id,par.proj_id,par.level,par.create_date,u.login, u.firstname, u.lastname,p.secondary_id,p.name,p.id,SUBSTRING(p.description,0,300) as description,pi.firstname || ' ' || pi.lastname FROM xs_par_table par LEFT JOIN xnat_projectData p ON par.proj_id=p.id LEFT JOIN xnat_investigatordata pi ON p.pi_xnat_investigatordata_id=pi.xnat_investigatordata_id LEFT JOIN xdat_user u ON par.approver_id=u.xdat_user_id WHERE LOWER(par.email)='"
									+ user.getEmail().toLowerCase()
									+ "' AND approval_date IS NULL", user
									.getDBName(), user.getLogin());

		} catch (Exception e) {
			e.printStackTrace();
		}

		MediaType mt = overrideVariant(variant);

		if (table != null)
			params.put("totalRecords", table.size());
		return representTable(table, mt, params);
	}
}
