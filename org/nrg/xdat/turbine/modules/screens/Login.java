// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

public class Login extends VelocitySecureScreen {
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
