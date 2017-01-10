/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_ArchiveHeaders
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;

public class XDATScreen_ArchiveHeaders extends SecureScreen {

	@Override
	protected void doBuildTemplate(RunData arg0, Context arg1) throws Exception {
		if (TurbineUtils.HasPassedParameter("numdays", arg0)) {
			arg1.put("numdays", TurbineUtils.GetPassedParameter("numdays", arg0));
		}
	}
}
