/*
 * org.nrg.xdat.turbine.modules.navigations.DefaultTop
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.navigations;
import org.apache.turbine.modules.navigations.VelocityNavigation;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFT;
/**
 * @author Tim
 *
 */
public class DefaultTop extends VelocityNavigation {
	protected void doBuildTemplate(RunData data,Context context)throws Exception
	{
		if (XDAT.getSiteConfigPreferences().getRequireLogin())
		{
			context.put("logout","true");
		}
	}
}

