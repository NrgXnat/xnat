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
