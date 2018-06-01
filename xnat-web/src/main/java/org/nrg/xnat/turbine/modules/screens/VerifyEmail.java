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
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xnat.security.XnatProviderManager;

@Slf4j
public class VerifyEmail extends org.nrg.xdat.turbine.modules.screens.VerifyEmail {
    @Override
    protected void doBuildTemplate(RunData data) throws Exception {
        final Context context = TurbineVelocity.getContext(data);
        SecureScreen.loadAdditionalVariables(data, context);
        context.put("login_methods", getProviderManager().getVisibleEnabledProviders().values());
        doBuildTemplate(data, context);
    }

    private XnatProviderManager getProviderManager() {
        if (_manager == null) {
            _manager = XDAT.getContextService().getBean(XnatProviderManager.class);
        }
        return _manager;
    }

    private static XnatProviderManager _manager;
}
