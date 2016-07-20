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
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;

@SuppressWarnings("unused")
public class XDATSudoLogin extends SecureAction {
    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        final UserI user = XDAT.getUserDetails();
        if (Roles.isSiteAdmin(user)) {
            final String login = (String) TurbineUtils.GetPassedParameter("sudo_login", data);
            final UserI su = Users.getUser(login);
            XDAT.loginUser(data, su, false);
        } else {
            notifyAdmin(user, data, 403, "Non-admin sudo attempt", "User attempted to sudo to another user account.");
        }
    }
}
