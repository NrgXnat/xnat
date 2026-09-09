/*
 * web: org.nrg.xnat.turbine.modules.screens.AdminSummary
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.screens.AdminScreen;
import org.nrg.xft.security.UserI;

public class ConfigureSearchViews extends AdminScreen {
	UserI u;
	@Override
	protected void doBuildTemplate(PipelineData pipelineData, Context context)
			throws Exception {
        RunData data = pipelineData.getRunData();

	}
}
