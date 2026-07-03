/*
 * core: org.nrg.xdat.turbine.modules.screens.Index
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
/**
 * @author Tim
 *
 */
public class Index extends SecureScreen {
	public void doBuildTemplate(PipelineData pipelineData, Context context)
	{
        final RunData data = pipelineData.getRunData();
        
	}
}

