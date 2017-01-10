/*
 * core: org.nrg.xdat.turbine.modules.actions.AdminAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.actions;

import org.apache.turbine.util.RunData;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AdminAction extends SecureAction {
	/* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.SecureScreen#isAuthorized(org.apache.turbine.util.RunData)
     */
    @Override
    protected boolean isAuthorized(RunData data) throws Exception {
        boolean authorized= super.isAuthorized(data);
        if (authorized)
        {
            if (!Roles.isSiteAdmin(TurbineUtils.getUser(data)))
            {
                authorized=false;
                data.setMessage("Unauthorized access.  Please login to gain access to this page.");
                logger.error("Unauthorized Access to an Admin Action (prevented).");
                AccessLogger.LogActionAccess(data, "Unauthorized access");
                AdminUtils.sendAdminEmail(TurbineUtils.getUser(data),"Unauthorized Admin Access Attempt", "Unauthorized Access to an Admin Action (" + data.getAction() +") prevented.");
                data.getResponse().sendError(403);
                
            }
        }
        
        return authorized;
    }

    private static final Logger logger = LoggerFactory.getLogger(AdminAction.class);
}
