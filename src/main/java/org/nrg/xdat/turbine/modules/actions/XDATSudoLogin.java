/*
 * org.nrg.xdat.turbine.modules.actions.XDATSudoLogin
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/30/13 5:36 PM
 */
package org.nrg.xdat.turbine.modules.actions;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

public class XDATSudoLogin extends SecureAction{

	@Override
	public void doPerform(RunData data, Context context) throws Exception {
		UserI user = TurbineUtils.getUser(data);
        if (user.checkRole("Administrator")) {
			String login = (String)TurbineUtils.GetPassedParameter("sudo_login", data);
			UserI temp=Users.getUser(login);
			TurbineUtils.setNewUser(data, temp, context);
            SecurityContextImpl securityContext = new SecurityContextImpl();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(temp, login, temp.getAuthorities());
            authentication.setDetails(SecurityContextHolder.getContext().getAuthentication().getDetails());
            securityContext.setAuthentication(authentication);
            data.getSession().setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
		}else{
			notifyAdmin(user, data, 403, "Non-admin sudo attempt", "User attempted to sudo to another user account.");
		}
	}

}
