/*
 * org.nrg.xnat.turbine.modules.screens.ManageProtocol
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
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xnat.services.ThemeService;


/**
 * Created by jcleve01 on 1/22/2016.
 */
public class XDATScreen_admin_options extends SecureScreen  {
    public final static Logger logger = Logger.getLogger(XDATScreen_admin_options.class);
    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        ThemeService themeService = XDAT.getContextService().getBean(ThemeService.class);
        String themedStyle = themeService.getThemePage("theme", "style");
        if (themedStyle != null) {
            context.put("themedStyle", themedStyle);
        }
        String themedScript = themeService.getThemePage("theme", "script");
        if (themedScript != null) {
            context.put("themedScript", themedScript);
        }
    }
}
