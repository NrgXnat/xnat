/*
 * web: org.nrg.xnat.turbine.modules.actions.XDATRegisterUser
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.security.XnatProviderManager;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.nrg.xnat.security.provider.XnatDatabaseAuthenticationProvider;
import org.nrg.xnat.security.tokens.XnatDatabaseUsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@SuppressWarnings("unused")
@Slf4j
public class RegisterExternalLogin extends XDATRegisterUser {
    public RegisterExternalLogin() {
        _service = XDAT.getContextService().getBean(XdatUserAuthService.class);
        _provider = XDAT.getContextService().getBean(XnatDatabaseAuthenticationProvider.class);
        _manager = XDAT.getContextService().getBean(XnatProviderManager.class);
    }

    @Override
    public void doPerform(final RunData data, final Context context) throws Exception {
        final String operation = (String) TurbineUtils.GetPassedParameter("operation", data);
        if (StringUtils.equals("merge", operation)) {
            // If it's a merge with an existing account, we need to validate the username and password.
            final String username = (String) TurbineUtils.GetPassedParameter("username", data);
            if (StringUtils.isBlank(username)) {
                retryAuthentication(data, context, "You must provide a valid username.");
                return;
            }
            if (StringUtils.equalsIgnoreCase("guest", username)) {
                retryAuthentication(data, context, "You can't associate an external authentication account with the guest user.");
                return;
            }

            final String password = (String) TurbineUtils.GetPassedParameter("password", data);
            if (StringUtils.isBlank(password)) {
                retryAuthentication(data, context, "You must provide a password value.");
                return;
            }

            try {
                final Authentication authentication = _provider.authenticate(new XnatDatabaseUsernamePasswordAuthenticationToken(username, password));

                // If the validation failed...
                if (!authentication.isAuthenticated()) {
                    // Let them know and try again.
                    retryAuthentication(data, context, "The submitted username and password didn't match an existing XNAT account.");
                    return;
                }
            } catch (AuthenticationException e) {
                log.info("User logged in with username {} through auth method {}, then tried to associate with existing XNAT account '{}' but provided invalid credentials", TurbineUtils.GetPassedParameter("authUsername", data), TurbineUtils.GetPassedParameter("authMethodId", data), username);
                retryAuthentication(data, context, "The submitted username and password didn't match an existing active XNAT account.");
                return;
            }
        } else {
            // If it's a new user, then run them through the standard registration workflow, but first
            // add any provider-specific properties, mainly auto-enable and auto-verify.
            final String authMethod   = data.getParameters().getString("authmethod");
            final String providerId = data.getParameters().getString("authmethodid");
            final XnatAuthenticationProvider provider = _manager.getProvider(authMethod, providerId);
            if (provider != null) {
                data.getParameters().add("authMethod", authMethod);
                data.getParameters().add("providerId", providerId);
                data.getParameters().add("providerAutoEnabled", Boolean.toString(provider.isAutoEnabled()));
                data.getParameters().add("providerAutoVerified", Boolean.toString(provider.isAutoVerified()));
            }
            super.doPerform(data, context);
        }

        createUserAuthRecord(data, operation);
    }

    @Override
    public void directRequest(final RunData data, final Context context, final UserI user) throws Exception {
        super.directRequest(data, context, user);
    }

    private void retryAuthentication(final RunData data, final Context context, final String message) {
        preserveVariables(data, context);
        data.setRedirectURI(null);
        data.setMessage("Authentication failed: " + message);
        data.setScreenTemplate("RegisterExternalLogin.vm");
    }

    private void createUserAuthRecord(final RunData data, final String operation) {
        final String authUsername = (String) TurbineUtils.GetPassedParameter("authUsername", data);
        final String authMethod   = (String) TurbineUtils.GetPassedParameter("authMethod", data);
        final String authMethodId = (String) TurbineUtils.GetPassedParameter("authMethodId", data);

        final XdatUserAuth auth = new XdatUserAuth(authUsername, authMethod, authMethodId);
        auth.setXdatUsername((String) TurbineUtils.GetPassedParameter(StringUtils.equals("merge", operation) ? "username" : "xdat:user.login", data));
        _service.create(auth);
    }

    private final XdatUserAuthService                _service;
    private final XnatDatabaseAuthenticationProvider _provider;
    private final XnatProviderManager                _manager;
}
