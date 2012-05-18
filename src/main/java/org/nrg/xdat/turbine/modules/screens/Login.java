// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xdat.turbine.modules.screens;

import com.google.common.collect.Maps;
import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.nrg.xdat.XDAT;
import org.nrg.xft.XFT;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;


public class Login extends VelocitySecureScreen {

	@Override
	protected void doBuildTemplate(RunData data) throws Exception {
		Context c = TurbineVelocity.getContext(data);
		String failed = (String)TurbineUtils.GetPassedParameter("failed", data);
		if(failed!=null && failed.equals("true")){
			data.setMessage("Login attempt failed. Please try again.");
		}
        SecureScreen.loadAdditionalVariables(data, c);
        List<AuthenticationProvider> prov = XDAT.getContextService().getBean("customAuthenticationManager",ProviderManager.class).getProviders();
        List<String> providerNames = new ArrayList<String>();
        for(AuthenticationProvider p : prov){
        	String name = p.toString();
        	if(!providerNames.contains(name)){
                if (isVisibleProvider(p)) {
        		    providerNames.add(name);
                }
        	}
        }

        c.put("login_methods", providerNames);
        doBuildTemplate(data, c);
	}

    private boolean isVisibleProvider(final AuthenticationProvider provider) {
        String name = provider.toString();
        if (!_providers.containsKey(name)) {
            Method[] methods = provider.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().equals("isVisible")) {
                    try {
                        _providers.put(name, (Boolean) method.invoke(provider));
                    } catch (IllegalAccessException exception) {
                        log.warn("Strange provider found with isVisible() method both accessible and inaccessible", exception);
                    } catch (InvocationTargetException exception) {
                        log.warn("Error invoking isVisible() method on provider", exception);
                    }
                }
            }
        }
        return _providers.get(name);
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


    private static final Map<String, Boolean> _providers = Maps.newConcurrentMap();
}
