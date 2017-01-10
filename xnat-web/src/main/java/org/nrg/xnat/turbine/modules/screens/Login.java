/*
 * web: org.nrg.xnat.turbine.modules.screens.Login
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
import org.nrg.xdat.XDAT;
import org.nrg.xdat.services.ThemeService;

public class Login extends org.nrg.xdat.turbine.modules.screens.Login {
    public final static Logger logger = Logger.getLogger(Login.class);
    @Override
    protected void doBuildTemplate(RunData data, Context c) throws Exception {
        ThemeService themeService = XDAT.getThemeService();
        String themedRedirect = themeService.getThemePage("Login");
        if(themedRedirect != null) {
            c.put("themedRedirect", themedRedirect);
            return;
        }
        String themedStyle = themeService.getThemePage("theme", "style");
        if(themedStyle != null) {
            c.put("themedStyle", themedStyle);
        }
        String themedScript = themeService.getThemePage("theme", "script");
        if(themedScript != null) {
            c.put("themedScript", themedScript);
        }
    }
}
