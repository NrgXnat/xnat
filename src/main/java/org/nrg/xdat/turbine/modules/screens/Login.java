// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import java.util.List;
import java.util.ArrayList;
import org.nrg.xdat.XDAT;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;


public class Login extends VelocitySecureScreen {

	@Override
	protected void doBuildTemplate(RunData data) throws Exception {
		Context c = TurbineVelocity.getContext(data);
        String systemName = TurbineUtils.GetSystemName();
        c.put("turbineUtils",TurbineUtils.GetInstance());
        c.put("systemName",systemName);
        List<AuthenticationProvider> prov = XDAT.getContextService().getBean("customAuthenticationManager",ProviderManager.class).getProviders();
        List<String> providerNames = new ArrayList<String>();
        for(AuthenticationProvider p : prov){
        	String name = p.toString();
        	if(!providerNames.contains(name)){
        		providerNames.add(name);
        	}
        }

        c.put("login_methods", providerNames);
        doBuildTemplate(data, c);
	}

	@Override
	protected void doBuildTemplate(RunData data, Context context) throws Exception {
		for(final Object param : data.getParameters().keySet()){
			final String paramS= (String)param;
			if ((!paramS.equalsIgnoreCase("template")) && (!paramS.equalsIgnoreCase("action"))){
				context.put(paramS,TurbineUtils.escapeParam(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(paramS,data))));
			}
		}
	}




	@Override
	protected boolean isAuthorized(RunData arg0) throws Exception {
		return false;
	}

}
