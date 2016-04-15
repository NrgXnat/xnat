/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Author: Justin Cleveland <clevelandj@wustl.edu> (jcleve01)
 * Last modified 1/22/2016 3:20 PM
 */
package org.nrg.xnat.turbine.modules.screens;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.services.ThemeService;

public class Login extends org.nrg.xdat.turbine.modules.screens.Login {
    public final static Logger logger = Logger.getLogger(XDATScreen_themes.class);
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