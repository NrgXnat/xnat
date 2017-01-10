/*
 * web: org.nrg.xnat.restlet.resources.ProjectPARListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.XFTTable;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ProjectAccessRequest;
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
public class ProjectPARListResource extends SecureResource {
	XnatProjectdata proj=null;

	public ProjectPARListResource(Context context, Request request, Response response) throws Exception {
		super(context, request, response);

		final UserI  user = getUser();
		final String pID  = (String) getParameter(request, "PROJECT_ID");

		if (pID != null) {
			proj = XnatProjectdata.getProjectByIDorAlias(pID, user, false);
		}

		if (proj != null) {
			getVariants().addAll(STANDARD_VARIANTS);

            if (!proj.canEdit(user)) {
                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "The user " + user.getLogin() + " is not allowed to access this information for the project: " + pID);
            }
		} else {
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unknown project: " + pID);
		}
	}


	@Override
	public Representation represent(Variant variant) {
		XFTTable table = new XFTTable();
		Hashtable<String,Object> params= new Hashtable<>();
		if (ProjectAccessRequest.CREATED_PAR_TABLE) {
			try {
				final UserI user = getUser();
				table = XFTTable
						.Execute(
								"SELECT par.par_id,par.proj_id,par.level,par.create_date,par.email,u.login,p.secondary_id,par.approved, par.approval_date FROM xs_par_table par LEFT JOIN xnat_projectData p ON par.proj_id=p.id LEFT JOIN xdat_user u ON par.approver_id=u.xdat_user_id WHERE par.proj_id='"
										+ proj.getId() + "'", user.getDBName(),
								user.getLogin());

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			table = new XFTTable();
			String[] headers = { "par_id", "proj_id", "level", "create_date",
					"email", "login", "secondary_id", "approved",
					"approval_date" };
			table.initTable(headers);
		}

		MediaType mt = overrideVariant(variant);

		return representTable(table, mt, params);
	}
}
