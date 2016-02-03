/*
 * org.nrg.xnat.turbine.modules.screens.PublicProjectView
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.screens;

import java.util.ArrayList;

import org.apache.turbine.modules.screens.VelocityScreen;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;

public class PublicProjectView extends VelocityScreen {

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.screens.VelocityScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        UserI user = TurbineUtils.getUser(data);
        
        if (user==null){
        	user=Users.getGuest();
            TurbineUtils.setUser(data, user);
        } 
        ArrayList allProjects = new ArrayList();
        
        for(XnatProjectdata p :XnatProjectdata.getAllXnatProjectdatas(user, false)){
            if (Permissions.can(user,p.getItem(), SecurityManager.ACTIVATE)){
                allProjects.add(p);
            }
        }
        
        context.put("projects", allProjects);
        
    }


}
