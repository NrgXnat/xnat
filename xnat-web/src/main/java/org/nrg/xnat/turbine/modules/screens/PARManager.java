/*
 * web: org.nrg.xnat.turbine.modules.screens.PARManager
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

/**
 * @author timo
 *
 */
public class PARManager extends SecureScreen {

	/* (non-Javadoc)
	 * @see org.apache.turbine.modules.screens.VelocitySecureScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
	 */
	@Override
	protected void doBuildTemplate(PipelineData pipelineData, Context context) throws Exception {
        RunData data = pipelineData.getRunData();
		
	}

}
