/*
 * web: org.nrg.xnat.turbine.modules.screens.PublicProjectView
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.modules.screens.VelocityScreen;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xft.security.UserI;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class PublicProjectView extends VelocityScreen {
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        UserI user = XDAT.getUserDetails();

        if (user == null) {
            XDAT.setGuestUserDetails();
        }

        final List<XnatProjectdata> allProjects = new ArrayList<>();

        for (XnatProjectdata p : XnatProjectdata.getAllXnatProjectdatas(user, false)) {
            if (Permissions.can(user, p.getItem(), SecurityManager.ACTIVATE)) {
                allProjects.add(p);
            }
        }

        context.put("projects", allProjects);
        SecureScreen.loadAdditionalVariables(data, context);
    }
}
