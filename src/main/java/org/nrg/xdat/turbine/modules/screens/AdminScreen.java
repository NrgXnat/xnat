//Copyright Washington University School of Medicine All Rights Reserved
/*
 * Created on Jan 30, 2007
 *
 */
package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;

public abstract class AdminScreen extends SecureScreen {

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
                logAccess(data,"Unauthorized access.");
                logger.error("Unauthorized Access to an Admin Screen (prevented).");
                AdminUtils.sendAdminEmail(TurbineUtils.getUser(data),"Unauthorized Admin Access Attempt", "Unauthorized Access to an Admin Screen (" + data.getScreen() +") prevented.");
                data.getResponse().sendError(403);
            }
        }
        
        return authorized;
    }


}
