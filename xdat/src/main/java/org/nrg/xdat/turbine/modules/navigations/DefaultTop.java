/*
 * core: org.nrg.xdat.turbine.modules.navigations.DefaultTop
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.navigations;
import org.apache.turbine.modules.navigations.VelocityNavigation;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFT;
/**
 * @author Tim
 *
 */
public class DefaultTop extends VelocityNavigation {
	protected void doBuildTemplate(PipelineData pipelineData,Context context)throws Exception
	{
        RunData data = pipelineData.getRunData();
		if (XDAT.getSiteConfigPreferences().getRequireLogin())
		{
			context.put("logout","true");
		}
		context.put("siteLogoPath", XDAT.getSiteLogoPath());
	}
}

