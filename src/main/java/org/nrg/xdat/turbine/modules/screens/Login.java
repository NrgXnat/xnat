/*
 * core: org.nrg.xdat.turbine.modules.screens.Login
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.screens;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistryImpl;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Login extends VelocitySecureScreen {
	@Override
	protected void doBuildTemplate(RunData data) throws Exception {
		String message = data.getMessage();
		
		if (!StringUtils.isBlank(message) && (message.startsWith("Password changed") || message.startsWith("Registration successful"))) {
		//If a user goes to the login page after changing their password, this logs them out.
			HttpSession session = data.getRequest().getSession(false);
	        if (session != null) {
	            session.invalidate();
	            if(XDAT.getContextService()!=null && XDAT.getContextService().getBean("sessionRegistry", SessionRegistryImpl.class)!=null){
			        SessionInformation si = XDAT.getContextService().getBean("sessionRegistry", SessionRegistryImpl.class).getSessionInformation(session.getId());
			        if (si!=null) {
			            si.expireNow();
			        }
		        }
	        }
	        SecurityContextHolder.clearContext();	
		}
		
		String failed = (String)TurbineUtils.GetPassedParameter("failed", data);
		
		Cookie[] cookies = data.getRequest().getCookies();
		boolean sessionTimedOut = false;
		boolean recentTimeout = false;
		Date logoutTime = new Date();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
				if (cookie.getName().equalsIgnoreCase("SESSION_TIMED_OUT")) {
					String val = cookie.getValue();
					if(StringUtils.equals(val,"true")){
						sessionTimedOut = true;
					}
				}
                if (cookie.getName().equalsIgnoreCase("SESSION_TIMEOUT_TIME")) {
                	String val = cookie.getValue();
                	if(val!=null && (!val.equals("")) && (cookie.getValue()!=null)){
                		logoutTime = (new Date(Long.parseLong(cookie.getValue())));
                		Date currTime = new Date();
                		//If their session timed out within the last 5 seconds, display a message to the user telling them that their session timed out.
                		if((currTime.getTime()-logoutTime.getTime())<5000){
							recentTimeout=true;
                		}

                	}
                }
            }
			if(sessionTimedOut&&recentTimeout){
				String messageTemplate = XDAT.getSiteConfigPreferences().getSessionTimeoutMessage();
				data.setMessage(messageTemplate.replaceAll("TIMEOUT_TIME",logoutTime.toString()));
			}
			else if(!StringUtils.isBlank(message) && message.startsWith("Session timed out at")){
				//If the message still says "Session timed out at ...", but they did not time out recently, reset it
				data.setMessage("");
			}
        }

		if(failed!=null && failed.equals("true")){
			data.setMessage(AdminUtils.GetLoginFailureMessage());
		}

		final Context context = TurbineVelocity.getContext(data);
        SecureScreen.loadAdditionalVariables(data, context);
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

        context.put("login_methods", providerNames);
        doBuildTemplate(data, context);
	}

    private boolean isVisibleProvider(final AuthenticationProvider provider) {
        String name = provider.toString();
        if (!_providers.containsKey(name)) {
            Method[] methods = provider.getClass().getMethods();
            Method foundMethod = null;
            for (Method method : methods) {
                if (method.getName().equals("isVisible")) {
                    foundMethod = method;
                }
            }
            if (foundMethod != null) {
                    try {
                    _providers.put(name, (Boolean) foundMethod.invoke(provider));
                    } catch (IllegalAccessException exception) {
                        log.warn("Strange provider found with isVisible() method both accessible and inaccessible", exception);
                    } catch (InvocationTargetException exception) {
                        log.warn("Error invoking isVisible() method on provider", exception);
                    }
            } else {
                // We default to assuming that, if a provider without isVisible() was added, that it's implicit, so don't show it.
                _providers.put(name, false);
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
