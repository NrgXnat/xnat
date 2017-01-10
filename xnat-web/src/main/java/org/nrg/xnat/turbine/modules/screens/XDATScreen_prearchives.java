/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_prearchives
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
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xnat.turbine.utils.XNATUtils;

import java.util.Hashtable;

@SuppressWarnings("unused")
public class XDATScreen_prearchives extends SecureScreen {
    /* (non-Javadoc)
     * @see org.apache.turbine.modules.screens.VelocityScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    protected void doBuildTemplate(final RunData data, final Context context) {
        try {
            final Hashtable hash = XNATUtils.getInvestigatorsForRead("xnat:mrSessionData", data);
            context.put("investigators", hash);

            if (data.getParameters().containsKey("project")) {
                context.put("project", org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project", data));
            }
            if (Roles.isSiteAdmin(XDAT.getUserDetails())) {
                context.put("role", "admin");
            }
        } catch (Exception e) {
            log.error(e);
        }
    }
}
