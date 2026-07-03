/*
 * core: org.nrg.xdat.turbine.modules.screens.Register
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;

public class ForgotLogin extends VelocitySecureScreen {


	@Override
	protected void doBuildTemplate(PipelineData pipelineData) throws Exception {
        final RunData data = pipelineData.getRunData();
		Context c = TurbineUtils.getVelocityContext(data);
        SecureScreen.loadAdditionalVariables(data, c);
        doBuildTemplate(data, c);
	}

	@Override
	protected void doBuildTemplate(PipelineData pipelineData, Context context) throws Exception {
        final RunData data = pipelineData.getRunData();
	}

	@Override
	protected boolean isAuthorized(PipelineData pipelineData) throws Exception {
        final RunData arg0 = pipelineData.getRunData();
		return false;
	}


}
