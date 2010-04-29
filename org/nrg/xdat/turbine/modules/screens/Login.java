// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;

public class Login extends VelocitySecureScreen {

	@Override
	protected void doBuildTemplate(RunData data) throws Exception {
		Context c = TurbineVelocity.getContext(data);
        String systemName = TurbineUtils.GetSystemName();
        c.put("turbineUtils",TurbineUtils.GetInstance());
        c.put("systemName",systemName);
        doBuildTemplate(data, c);
	}

	@Override
	protected void doBuildTemplate(RunData data, Context context) throws Exception {
		for(final Object param : data.getParameters().keySet()){
			final String paramS= (String)param;
			if ((!paramS.equalsIgnoreCase("template")) && (!paramS.equalsIgnoreCase("action"))){
				context.put(paramS,data.getParameters().get(paramS));
			}
		}
	}




	@Override
	protected boolean isAuthorized(RunData arg0) throws Exception {
		return false;
	}

}
