/*
 * web: org.nrg.xnat.restlet.resources.UserListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.security.UserI;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.sql.SQLException;
import java.util.Hashtable;

public class UserListResource extends SecureResource {
	XFTTable table = null;

	public UserListResource(Context context, Request request, Response response) {
		super(context, request, response);

        getVariants().addAll(STANDARD_VARIANTS);

        final UserI user = getUser();

        if (user.isGuest() || restrictUserListAccessToAdmins() && !(Roles.isSiteAdmin(user) || isWhitelisted())) {
                logger.error("Unauthorized Access to site-level user resources. User: " + userName);
                this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Access Denied: Only site managers can access site-level user resources.");
            }
        String query = "SELECT xdat_user_id,login,firstname,lastname,email FROM xdat_user WHERE enabled=1 ORDER BY lastname;";
			try {
				table = XFTTable.Execute(query, user.getDBName(), user.getLogin());
			} catch (SQLException e) {
            logger.error("Error running SQL " + query, e);
			} catch (DBPoolException e) {
            logger.error("Connection pooling error occurred", e);
		}
	}

	@Override
	public boolean allowGet() {
		return true;
	}

	@Override
	public Representation represent(Variant variant) {
		Hashtable<String,Object> params= new Hashtable<>();
		params.put("title", "Projects");

		MediaType mt = overrideVariant(variant);

        String query = "SELECT xdat_user_id,login,firstname,lastname,email FROM xdat_user WHERE enabled=1 ORDER BY lastname;";
        try {
            final UserI user = getUser();
            table = XFTTable.Execute(query, user.getDBName(), user.getLogin());
        } catch (SQLException e) {
            logger.error("Error running SQL " + query, e);
        } catch (DBPoolException e) {
            logger.error("Connection pooling error occurred", e);
        }

		if(table!=null)params.put("totalRecords", table.size());
		return this.representTable(table, mt, params);
	}

    /**
     * This wraps a call to the site configuration service to determine if access to the user list function should be
     * restricted to site administrators. This method defaults to true, which provides the highest level of security.
     * Note that this does <i>not</i> mean that the site settings default to <b>true</b>, just that if the site setting
     * can not be accessed, this method will default to the most restrictive level of access.
     *
     * @return <b>true</b> if only site administrators can access the list of users, <b>false</b> otherwise.
     */
    private boolean restrictUserListAccessToAdmins() {
        return XDAT.getSiteConfigPreferences().getRestrictUserListAccessToAdmins();
    }
}
