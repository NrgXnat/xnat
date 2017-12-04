/*
 * web: org.nrg.xnat.turbine.modules.screens.Index
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import lombok.extern.slf4j.Slf4j;
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
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.turbine.utils.ProjectAccessRequest;

import java.util.Date;
import java.util.Map;

@Slf4j
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

        if (TurbineUtils.GetPassedParameter("node", data) != null) {
            context.put("node", TurbineUtils.GetPassedParameter("node", data));
        }

        ProjectAccessRequest.CreatePARTable();

        if (StringUtils.isBlank(user.getEmail())) {
            data.setMessage("WARNING: A valid email account is required for many features.  Please use the (edit) link at the top of the page to add a valid email address to your user account.");
        } else {
            final Integer parCount = (Integer) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(par_id)::int4 AS count FROM xs_par_table WHERE approval_date IS NULL AND LOWER(email)='" + user.getEmail().toLowerCase() + "'", "count", user.getDBName(), user.getLogin());
            log.debug("Found {} outstanding project access requests for user {}", parCount, user.getUsername());
            context.put("par_count", parCount);
        }

        final UserHelperServiceI userHelperService = UserHelper.getUserHelperService(user);

        final Date lastLogin = userHelperService.getPreviousLogin();
        if (lastLogin != null) {
            context.put("last_login", lastLogin);
        }

        final Map totalCounts = userHelperService.getTotalCounts();
        final Object projectCount        = totalCounts.get("xnat:projectData");
        final Object subjectCount = totalCounts.get("xnat:subjectData");
        final Long imageSessionCount = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM xnat_imageSessionData", "count", user.getDBName(), user.getUsername());

        context.put("proj_count", projectCount);
        context.put("sub_count", subjectCount);
        context.put("isd_count", imageSessionCount);

        log.debug("Found {} projects, {} subjects, and {} image sessions for the user {}", projectCount, subjectCount, imageSessionCount);

        //count prearc entries
        try {
            final int prearcSessionCount = PrearcDatabase.buildRows(user, null).size();
            log.debug("Found {} prearchive sessions for the user {}", prearcSessionCount);
            context.put("prearc_count", prearcSessionCount);
        } catch (Throwable e) {
            logger.error("", e);
        }
    }
}
