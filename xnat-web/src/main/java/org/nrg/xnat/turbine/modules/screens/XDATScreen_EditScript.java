/*
 * org.nrg.xnat.turbine.modules.screens.XDATScreen_EditScript
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;

public class XDATScreen_EditScript extends SecureScreen {

	@Override
	protected void doBuildTemplate(RunData arg0, Context arg1) throws Exception {
		arg1.put("user", TurbineUtils.getUser(arg0).getUsername());
		if (Roles.isSiteAdmin(TurbineUtils.getUser(arg0))) {
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
