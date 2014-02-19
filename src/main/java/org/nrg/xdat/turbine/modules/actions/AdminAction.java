/*
 * org.nrg.xdat.turbine.modules.actions.AdminAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/9/13 1:06 PM
 */
package org.nrg.xdat.turbine.modules.actions;

import org.apache.turbine.util.RunData;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;

public abstract class AdminAction extends SecureAction {
	/* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.SecureScreen#isAuthorized(org.apache.turbine.util.RunData)
     */
    @Override
    protected boolean isAuthorized(RunData data) throws Exception {
        boolean authorized= super.isAuthorized(data);
        if (authorized)
        {
            if (!TurbineUtils.getUser(data).checkRole("Administrator"))
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

}
