/*
 * org.nrg.xnat.turbine.modules.screens.ManageProtocol
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
import org.nrg.xnat.services.ThemeService;

public class Login extends org.nrg.xdat.turbine.modules.screens.Login {
    public final static Logger logger = Logger.getLogger(XDATScreen_themes.class);
    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        ThemeService themeService = XDAT.getContextService().getBean(ThemeService.class);
//        String themedLoginPath = themeService.getThemePage("Login");
//        if(themedLoginPath != null) {
//            doRedirect(data, themedLoginPath);
//            data.setRedirectURI(themedLoginPath);
//        }
        String themedRedirect = themeService.getThemePage("Login");
        if(themedRedirect != null) {
            context.put("themedRedirect", themedRedirect);
            return;
        }
        String themedStyle = themeService.getThemePage("theme", "style");
        if(themedStyle != null) {
            context.put("themedStyle", themedStyle);
        }
        String themedScript = themeService.getThemePage("theme", "script");
        if(themedScript != null) {
            context.put("themedScript", themedScript);
        }
    }
}