/*
 * core: org.nrg.xdat.turbine.modules.screens.Register
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;

public class Register extends VelocitySecureScreen {


	@Override
	protected void doBuildTemplate(RunData data) throws Exception {
		Context c = TurbineVelocity.getContext(data);
        SecureScreen.loadAdditionalVariables(data, c);
        doBuildTemplate(data, c);
	}

	@Override
	protected void doBuildTemplate(RunData data, Context context) throws Exception {
		SiteConfigPreferences siteConfig  = XDAT.getSiteConfigPreferences();
		if(siteConfig.getSecurityNewUserRegistrationDisabled()){
			data.setMessage("New user registration is not allowed on " + siteConfig.getSiteId());
			UserI u = XDAT.getUserDetails();
			if((u != null && !u.getUsername().equals("guest")) || !siteConfig.getRequireLogin()){
				data.setScreenTemplate("Index.vm");
				return;
			}
			data.setScreenTemplate("Login.vm");
			return;
		}

		for(final Object param : data.getParameters().keySet()){
			final String paramS= (String)param;
			if ((!paramS.equalsIgnoreCase("template"))
					&& (!paramS.equalsIgnoreCase("action"))
					&& (!paramS.equalsIgnoreCase("username"))
					&& (!paramS.equalsIgnoreCase("password"))){
				context.put(paramS,TurbineUtils.escapeParam(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(paramS,data))));
			}
		}
	}

	@Override
	protected boolean isAuthorized(RunData arg0) throws Exception {
		return false;
	}


}
