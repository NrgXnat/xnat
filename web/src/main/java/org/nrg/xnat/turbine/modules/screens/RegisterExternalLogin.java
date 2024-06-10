/*
 * web: org.nrg.xnat.turbine.modules.screens.RegisterExternalLogin
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2018, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.exceptions.UsernameAuthMappingNotFoundException;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;

@SuppressWarnings("unused")
@Slf4j
public class RegisterExternalLogin extends VelocitySecureScreen {
    @Override
    protected void doBuildTemplate(final RunData data) throws Exception {
        final Context context = TurbineVelocity.getContext(data);
        SecureScreen.loadAdditionalVariables(data, context);

        doBuildTemplate(data, context);
    }

    @Override
    protected void doBuildTemplate(final RunData data, final Context context) throws Exception {
        final UsernameAuthMappingNotFoundException exception = (UsernameAuthMappingNotFoundException) data.getRequest().getSession().getAttribute(UsernameAuthMappingNotFoundException.class.getSimpleName());
        if (exception == null) {
            TurbineUtils.redirectToLogin(data);
            return;
        }

        context.put("data", data);
        context.put("siteConfig", XDAT.getSiteConfigPreferences());
        context.put("userInfo", exception);

        for (final Object object : data.getParameters().keySet()) {
            final String parameter = (String) object;
            if (!StringUtils.equalsAnyIgnoreCase(parameter, "template", "action")) {
                context.put(parameter, TurbineUtils.escapeParam((String) TurbineUtils.GetPassedParameter(parameter, data)));
            }
        }
    }

    @Override
    protected boolean isAuthorized(final RunData data) {
        return false;
    }
}
