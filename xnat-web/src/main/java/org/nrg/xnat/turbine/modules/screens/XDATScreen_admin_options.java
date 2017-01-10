/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_admin_options
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;

public class XDATScreen_admin_options extends SecureScreen  {
    public final static Logger logger = Logger.getLogger(XDATScreen_admin_options.class);
    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
    }
}
