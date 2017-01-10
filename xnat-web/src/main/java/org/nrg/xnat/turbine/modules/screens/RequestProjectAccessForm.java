/*
 * web: org.nrg.xnat.turbine.modules.screens.RequestProjectAccessForm
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;

public class RequestProjectAccessForm extends SecureScreen {
    private XnatProjectdata project = null;
    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        String p = ((String) TurbineUtils.GetPassedParameter("project",data));
        UserI user = XDAT.getUserDetails();
        if (project==null){
            project = XnatProjectdata.getXnatProjectdatasById(p, user, false);
        }
        
        if (!Permissions.canDelete(user, project)){
            data.setMessage("Permission Denied.  The current user is not authorized to view this data.");
            data.setScreenTemplate("UnauthorizedAccess.vm");
            return;
        } 
        
        String access_level = ((String) TurbineUtils.GetPassedParameter("access_level",data));
        Integer id = TurbineUtils.GetPassedInteger("id", data);
        UserI other =Users.getUser(id);
        
        context.put("user", other);
        context.put("projectOM", project);
        context.put("access_level", access_level);
    }
    
    @Override
    public boolean allowGuestAccess() {
        return false;
    }
}
