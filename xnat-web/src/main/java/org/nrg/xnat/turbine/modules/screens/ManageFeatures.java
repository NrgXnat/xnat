//Copyright 2012 Radiologics, Inc
//Author: Tim Olsen <tim@radiologics.com>
package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;

import org.apache.velocity.context.Context;
import org.nrg.xdat.security.helpers.Features;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;

/**
 * @author tim@radiologics.com
 *
 * Prep class for the interactive dashboard (Quick View).  Doesn't do anything but basic initialization (security)
 */
public class ManageFeatures extends SecureScreen {

	@Override
	protected void doBuildTemplate(RunData data, Context context) throws Exception {
		context.put("features", Features.getAllFeatures());
	}

}
