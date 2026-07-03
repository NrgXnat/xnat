/*
 * web: org.nrg.xnat.turbine.modules.screens.UploadOptions
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

@SuppressWarnings("unused")
public class UploadOptions extends DICOMSCPPage {
	@Override
	protected void doBuildTemplate(final PipelineData pipelineData, final Context context) throws Exception {
        final RunData data = pipelineData.getRunData();
		super.doBuildTemplate(data, context);
	}
}
