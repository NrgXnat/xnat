// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xdat.turbine.modules.actions;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.TurbineUtils;

public class XDATSudoLogin extends SecureAction{

	@Override
	public void doPerform(RunData data, Context context) throws Exception {
		XDATUser user = TurbineUtils.getUser(data);
		if (user.checkRole("Administrator"))
		{
			String login = (String)TurbineUtils.GetPassedParameter("sudo_login", data);
			XDATUser temp = new XDATUser(login);
			
			TurbineUtils.setUser(data, temp);
		}
	}

}
