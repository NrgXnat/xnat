/*
 * web: org.nrg.xnat.turbine.modules.screens.Index
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ProjectAccessRequest;

import java.util.Date;

public class Index extends SecureScreen {

    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        final String themedRedirect = themeService.getThemePage("Landing");           // TODO: put all this in a method in the theme service with an optional requested page parameter
        if (StringUtils.isNotBlank(themedRedirect)) {
            context.put("themedRedirect", themedRedirect);
            return;
        }

        final UserI user = XDAT.getUserDetails();
        assert user != null;

        final UserHelperServiceI userHelper = UserHelper.getUserHelperService(user);
        assert userHelper != null;

        if (TurbineUtils.GetPassedParameter("node", data) != null) {
            context.put("node", TurbineUtils.GetPassedParameter("node", data));
        }

        ProjectAccessRequest.CreatePARTable();

        if (StringUtils.isBlank(user.getEmail())) {
            data.setMessage("WARNING: A valid email account is required for many features.  Please use the (edit) link at the top of the page to add a valid email address to your user account.");
        } else {
            context.put("par_count", PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(par_id)::int4 AS count FROM xs_par_table WHERE approval_date IS NULL AND LOWER(email)='" + user.getEmail().toLowerCase() + "'", "count", user.getDBName(), user.getLogin()));
        }

        final Date lastLogin = userHelper.getPreviousLogin();
        if (lastLogin != null) {
            context.put("last_login", lastLogin);
        }

        context.put("proj_count", userHelper.getTotalCounts().get("xnat:projectData"));
        context.put("sub_count", userHelper.getTotalCounts().get("xnat:subjectData"));
        context.put("isd_count", PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM xnat_imageSessionData", "count", user.getDBName(), user.getUsername()));

        //count prearc entries
        /*
		try {
			context.put("prearc_count",PrearcDatabase.buildRows(user, null).size());
		} catch (Throwable e) {
			logger.error("",e);
		}
		*/
    }
}
