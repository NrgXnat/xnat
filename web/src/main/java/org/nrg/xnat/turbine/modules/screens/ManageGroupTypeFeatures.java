/*
 * web: org.nrg.xnat.turbine.modules.screens.ManageGroupTypeFeatures
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.helpers.Features;
import org.nrg.xdat.turbine.modules.screens.AdminScreen;

public class ManageGroupTypeFeatures extends AdminScreen {

	@Override
	protected void doBuildTemplate(RunData data, Context context) throws Exception {
        context.put("features", Features.getAllFeatures());
	}

}
