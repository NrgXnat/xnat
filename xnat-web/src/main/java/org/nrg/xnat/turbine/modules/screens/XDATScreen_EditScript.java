/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_EditScript
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;

public class XDATScreen_EditScript extends SecureScreen {

	@Override
	protected void doBuildTemplate(RunData arg0, Context arg1) throws Exception {
		final UserI user = XDAT.getUserDetails();
		arg1.put("user", user.getUsername());
		if (Roles.isSiteAdmin(user)) {
			arg1.put("isAdmin","true");
		}
		else {
			arg1.put("isAdmin", "false");
		}
		if (arg0.getParameters().containsKey("project")) {
			arg1.put("project", arg0.getParameters().getObject("project"));
		}		
	}
}
