/*
 * org.nrg.xdat.turbine.modules.screens.XDATScreen_add_xdat_element_security
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

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.ElementSecurity;

/**
 * @author Tim
 *
 */
public class XDATScreen_add_xdat_element_security extends AdminScreen {

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.screens.VelocityScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    protected void doBuildTemplate(RunData data, Context context)
            throws Exception {
        ArrayList allAbsent = ElementSecurity.GetAbsentElements();
        context.put("elements",allAbsent);
    }

}
