/*
 * web: org.nrg.xnat.turbine.modules.screens.RequestProjectAccess
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;

public class RequestProjectAccess extends SecureScreen {

    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        String p = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project",data));
        XnatProjectdataI project = XnatProjectdata.getXnatProjectdatasById(p, null, false);
        
        context.put("project", project);
    }
    
    public boolean allowGuestAccess(){
        return false;
    }
}
