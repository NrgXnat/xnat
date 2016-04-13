/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Author: Justin Cleveland <clevelandj@wustl.edu>
 * Last modified 1/22/2016 3:20 PM
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;

public class XDATScreen_themes extends SecureScreen  {
    public final static Logger logger = Logger.getLogger(XDATScreen_themes.class);
    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
    }
}
