/*
 * org.nrg.xdat.turbine.modules.screens.XDATScreen_secure_access_xdat_element_security
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.screens;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XdatElementSecurity;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.TurbineUtils;

/**
 * @author Tim
 *
 */
public class XDATScreen_secure_access_xdat_element_security extends
        AdminReport {

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void finalProcessing(RunData data, Context context) {
        ArrayList allUsers = new ArrayList();
        XDATUser primaryUser =TurbineUtils.getUser(data);
        XdatElementSecurity es = (XdatElementSecurity)om;
        Iterator users = XdatUser.getAllXdatUsers(null,false).iterator();
        while(users.hasNext())
        {
            try {
                XDATUser u = new XDATUser(((XdatUser)users.next()).getItem());
                allUsers.add(u);
                
            } catch (Exception e) {
                logger.error("",e);
            }
        }
        
        context.put("allUsers",allUsers);
    }

}
