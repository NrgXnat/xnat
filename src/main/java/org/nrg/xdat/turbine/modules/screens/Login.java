/*
 * core: org.nrg.xdat.turbine.modules.screens.Login
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.screens;

import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class Login extends VelocitySecureScreen {
	@Override
	protected void doBuildTemplate(RunData data) throws Exception {
		final String message = data.getMessage();

		if (!StringUtils.isBlank(message) && (message.startsWith("Password changed") || message.startsWith("Registration successful"))) {
		//If a user goes to the login page after changing their password, this logs them out.
			final HttpSession session = data.getRequest().getSession(false);
	        if (session != null) {
	            session.invalidate();
	            if(XDAT.getContextService()!=null && XDAT.getContextService().getBean("sessionRegistry", SessionRegistryImpl.class)!=null){
			        final SessionInformation sessionInfo = XDAT.getContextService().getBean("sessionRegistry", SessionRegistryImpl.class).getSessionInformation(session.getId());
			        if (sessionInfo!=null) {
			            sessionInfo.expireNow();
			        }
		        }
	        }
	        SecurityContextHolder.clearContext();
		}

		final String failed = (String)TurbineUtils.GetPassedParameter("failed", data);

		final Cookie[] cookies = data.getRequest().getCookies();
		boolean sessionTimedOut = false;
		boolean recentTimeout = false;
		Date logoutTime = new Date();
        if (cookies != null) {
            for (final Cookie cookie : cookies) {
				if (cookie.getName().equalsIgnoreCase("SESSION_TIMED_OUT")) {
					if (StringUtils.equals(cookie.getValue(), "true")){
						sessionTimedOut = true;
					}
				}
                if (cookie.getName().equalsIgnoreCase("SESSION_TIMEOUT_TIME")) {
                	final String value = cookie.getValue();
                	if(StringUtils.isNotBlank(value)){
                		logoutTime = (new Date(Long.parseLong(cookie.getValue())));
                		//If their session timed out within the last 5 seconds, display a message to the user telling them that their session timed out.
                		if ((new Date().getTime() - logoutTime.getTime()) < 5000) {
							recentTimeout = true;
                		}
                	}
                }
            }
			if (sessionTimedOut && recentTimeout) {
				String messageTemplate = XDAT.getSiteConfigPreferences().getSessionTimeoutMessage();
				data.setMessage(messageTemplate.replaceAll("TIMEOUT_TIME", logoutTime.toString()));
			} else if (!StringUtils.isBlank(message) && message.startsWith("Session timed out at")) {
				//If the message still says "Session timed out at ...", but they did not time out recently, reset it
				data.setMessage("");
			}
        }

		if(failed!=null && failed.equals("true")){
			data.setMessage(AdminUtils.GetLoginFailureMessage());
		}

		final Context context = TurbineVelocity.getContext(data);
        SecureScreen.loadAdditionalVariables(data, context);

        doBuildTemplate(data, context);
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
	protected boolean isAuthorized(final RunData data) {
		return false;
	}
}
