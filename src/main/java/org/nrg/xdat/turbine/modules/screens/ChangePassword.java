// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xdat.turbine.modules.screens;

import org.apache.commons.lang.StringUtils;
import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;

public class ChangePassword extends VelocitySecureScreen {

    @Override
    protected void doBuildTemplate(RunData data) throws Exception {
        Context c = TurbineVelocity.getContext(data);
        SecureScreen.loadAdditionalVariables(data, c);
        doBuildTemplate(data, c);
    }

    @Override
    protected void doBuildTemplate(final RunData data, final Context context) {
        try {
            if (data != null && TurbineUtils.getUser(data) != null &&
                    !StringUtils.isBlank(TurbineUtils.getUser(data).getUsername()) &&
                    !TurbineUtils.getUser(data).getUsername().equalsIgnoreCase("guest")) {
                context.put("login", TurbineUtils.getUser(data).getUsername());
                context.put("topMessage", "Your password has expired. Please choose a new one.");
            } else {
                String alias = (String) TurbineUtils.GetPassedParameter("a", data);
                String secret = (String) TurbineUtils.GetPassedParameter("s", data);

                context.put("a", alias);
                context.put("s", secret);
                context.put("topMessage", "Please choose a new password.");
            }
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        }
    }

    @Override
    protected boolean isAuthorized(RunData arg0) throws Exception {
        return false;
    }
}
