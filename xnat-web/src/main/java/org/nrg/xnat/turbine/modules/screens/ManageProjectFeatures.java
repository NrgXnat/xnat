package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.helpers.Features;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;

public class ManageProjectFeatures extends SecureScreen {
	@Override
	protected void doBuildTemplate(RunData data, Context context) throws Exception {
        final Object projects = TurbineUtils.GetPassedParameter("projects", data);
        if (projects != null) {
            context.put("project", projects);
        } else {
            context.put("project", TurbineUtils.GetPassedParameter("project", data));
        }
        context.put("features", Features.getAllFeatures());
	}
}
