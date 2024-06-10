/*
 * web: org.nrg.xnat.turbine.modules.screens.DataManagementReport
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.turbine.modules.screens.SecureReport;
import org.nrg.xdat.turbine.utils.TurbineUtils;

public abstract class DataManagementReport extends SecureReport {

    static Logger logger = Logger.getLogger(DataManagementReport.class);
    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.SecureScreen#isAuthorized(org.apache.turbine.util.RunData)
     */
    @Override
    protected boolean isAuthorized(RunData data) throws Exception {
        boolean authorized= super.isAuthorized(data);
        if (authorized)
        {
//            if (!TurbineUtils.getUser(data).checkRole("DataManager"))
//            {
//                authorized=false;
//                data.setMessage("Unauthorized access.  Please login to gain access to this page.");
//                logAccess(data,"Unauthorized access.");
//                logger.error("Unauthorized Access to an DataManager Report (prevented).");
//            }
        }
        
        return authorized;
    }
}
