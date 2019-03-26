/*
 * web: org.nrg.xnat.turbine.modules.screens.Login
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.services.ThemeService;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.security.XnatProviderManager;

@Slf4j
public class Login extends org.nrg.xdat.turbine.modules.screens.Login {
    @Override
    protected void doBuildTemplate(final RunData data, final Context context) throws Exception {
        context.put("login_methods", getProviderManager().getVisibleEnabledProviders().values());

        final ThemeService themeService   = XDAT.getThemeService();
        final String       themedRedirect = themeService.getThemePage("Login");
        if (StringUtils.isNotBlank(themedRedirect)) {
            context.put("themedRedirect", themedRedirect);
            return;
        }
        String themedStyle = themeService.getThemePage("theme", "style");
        if (themedStyle != null) {
            context.put("themedStyle", themedStyle);
        }
        String themedScript = themeService.getThemePage("theme", "script");
        if (themedScript != null) {
            context.put("themedScript", themedScript);
        }
        
        // Redirect to Index.vm if the user is already logged in
        UserI u = XDAT.getUserDetails();
        if(null != u && !u.getUsername().equalsIgnoreCase("guest")){
            data.setScreenTemplate("Index.vm");
        }
    }

    private XnatProviderManager getProviderManager() {
        if (_manager == null) {
            _manager = XDAT.getContextService().getBean(XnatProviderManager.class);
        }
        return _manager;
    }

    private static XnatProviderManager _manager;
}
