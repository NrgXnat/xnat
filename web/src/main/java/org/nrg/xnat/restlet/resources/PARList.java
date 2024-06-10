/*
 * web: org.nrg.xnat.restlet.resources.PARList
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.nrg.xft.XFTTable;
import org.nrg.xft.security.UserI;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

/**
 * @author timo
 * 
 */
public class PARList extends SecureResource {
	public PARList(Context context, Request request, Response response) {
		super(context, request, response);
		getVariants().addAll(STANDARD_VARIANTS);
	}

	@Override
	public Representation represent(Variant variant) {
		final UserI                     user   = getUser();
		final Hashtable<String, Object> params = new Hashtable<>();
		try {
			final XFTTable table = XFTTable.Execute(String.format(PAR_QUERY, user.getEmail().toLowerCase()), user.getDBName(), user.getLogin());

			if (table != null) {
				params.put("totalRecords", table.size());
			}

			return representTable(table, overrideVariant(variant), params);

		} catch (Exception e) {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "An error occurred attempting to access the project invitations.");
			_log.error("An error occurred attempting to access the project invitations for user " + user.getLogin(), e);
		}

		return null;
	}
	private static final Logger _log = LoggerFactory.getLogger(PARList.class);
	private static final String PAR_QUERY = "SELECT par.par_id,par.proj_id,par.level,par.create_date,u.login, u.firstname, u.lastname,p.secondary_id,p.name,p.id,SUBSTRING(p.description,0,300) as description,pi.firstname || ' ' || pi.lastname FROM xs_par_table par LEFT JOIN xnat_projectData p ON par.proj_id=p.id LEFT JOIN xnat_investigatordata pi ON p.pi_xnat_investigatordata_id=pi.xnat_investigatordata_id LEFT JOIN xdat_user u ON par.approver_id=u.xdat_user_id WHERE LOWER(par.email)='%s' AND approval_date IS NULL";
}
