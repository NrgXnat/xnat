/*
 * core: org.nrg.xdat.turbine.modules.screens.AdminReport
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;

public abstract class AdminReport extends SecureReport {

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
                logAccess(data,"Unauthorized access.");
                logger.error("Unauthorized Access to an Admin Screen (prevented).");
                AdminUtils.sendAdminEmail(TurbineUtils.getUser(data),"Unauthorized Admin Access Attempt", "Unauthorized Access to an Admin Screen (" + data.getScreen() +") prevented.");
                
                
            }
        }
        
        return authorized;
    }

}
