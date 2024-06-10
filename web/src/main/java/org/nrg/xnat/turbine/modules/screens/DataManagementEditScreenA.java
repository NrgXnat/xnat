/*
 * web: org.nrg.xnat.turbine.modules.screens.DataManagementEditScreenA
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.turbine.modules.screens.EditScreenA;
import org.nrg.xdat.turbine.utils.TurbineUtils;

public abstract class DataManagementEditScreenA extends EditScreenA {
    static Logger logger = Logger.getLogger(DataManagementEditScreenA.class);
    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.SecureScreen#isAuthorized(org.apache.turbine.util.RunData)
     */
    @Override
    protected boolean isAuthorized(RunData data) throws Exception {
        boolean authorized= super.isAuthorized(data);
//      elimnated DataManager role because every user was automatically one.  
//        if (authorized)
//        {
//            if (!TurbineUtils.getUser(data).checkRole("DataManager"))
//            {
//                authorized=false;
//                data.setMessage("Unauthorized access.  Please login to gain access to this page.");
//                logAccess(data,"Unauthorized access.");
//                logger.error("Unauthorized Access to an DataManager Report (prevented).");
//            }
//        }
        
        return authorized;
    }

}
