/*
 * web: org.nrg.xnat.turbine.modules.screens.UploadAssistantPage
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
import org.nrg.xdat.turbine.modules.screens.SecureScreen;

public class UploadAssistantPage extends SecureScreen {

	@Override
	protected void doBuildTemplate(PipelineData pipelineData, Context context) throws Exception {
        final RunData data = pipelineData.getRunData();
		// doesn't currently need any context, just needed to subclass SecureScreen
	}

}
