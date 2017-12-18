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
import org.nrg.xnat.security.provider.XnatDatabaseAuthenticationProvider;
import org.nrg.xnat.security.tokens.XnatDatabaseUsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@SuppressWarnings("unused")
@Slf4j
public class RegisterExternalLogin extends XDATRegisterUser {
    public RegisterExternalLogin() {
        _service = XDAT.getContextService().getBean(XdatUserAuthService.class);
        _provider = XDAT.getContextService().getBean(XnatDatabaseAuthenticationProvider.class);
    }

    @Override
    public void doPerform(final RunData data, final Context context) throws Exception {
        final String operation = (String) TurbineUtils.GetPassedParameter("operation", data);
        if (StringUtils.equals("merge", operation)) {
            // If it's a merge with an existing account, we need to validate the username and password.
            final String username = (String) TurbineUtils.GetPassedParameter("username", data);
            final String password = (String) TurbineUtils.GetPassedParameter("password", data);
            final Authentication authentication = _provider.authenticate(new XnatDatabaseUsernamePasswordAuthenticationToken(username, password));

            // If the validation failed...
            if (!authentication.isAuthenticated()) {
                // Display an error message and route them back to the register page.
                context.put("errorMessage", "The submitted username and password didn't match an existing XNAT account.");
                data.getResponse().sendRedirect(TurbineUtils.GetFullServerPath() + "/app/template/RegisterExternalLogin.vm");
                return;
            }
        } else {
            // If it's a new user, then run them through the standard registration workflow.
            super.doPerform(data, context);
        }

        createUserAuthRecord(data, operation);
    }

    @Override
    public void directRequest(RunData data, Context context, UserI user) throws Exception {
        super.directRequest(data, context, user);
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
}
