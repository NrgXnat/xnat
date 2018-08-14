/*
 * core: org.nrg.xdat.turbine.modules.actions.AdminAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.actions;

import lombok.extern.slf4j.Slf4j;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.security.UserI;

@Slf4j
public abstract class AdminAction extends SecureAction {
    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.SecureScreen#isAuthorized(org.apache.turbine.util.RunData)
     */
    @Override
    protected boolean isAuthorized(RunData data) throws Exception {
        if (!super.isAuthorized(data)) {
            return false;
        }

        final UserI user = getUser();
        if (Roles.isSiteAdmin(user)) {
            return true;
        }

        data.setMessage("Unauthorized access.  Please login to gain access to this page.");
        log.error("Unauthorized Access to an Admin Action (prevented).");
        AccessLogger.LogActionAccess(data, "Unauthorized access");
        AdminUtils.sendAdminEmail(user, "Unauthorized Admin Access Attempt", "Unauthorized Access to an Admin Action (" + data.getAction() + ") prevented.");
        data.getResponse().sendError(403);
        return false;
    }
}
