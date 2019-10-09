/*
 * web: org.nrg.xnat.turbine.modules.screens.RequestProjectAccess
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import lombok.extern.slf4j.Slf4j;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.modules.screens.XdatScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;

@XdatScreen
@Slf4j
public class RequestProjectAccess extends SecureScreen {
    @Override
    protected void doBuildTemplate(final RunData data, final Context context) throws Exception {
        final String projectId = (String) TurbineUtils.GetPassedParameter("project", data);
        log.debug("User {} is requesting access to project {}", XDAT.getUserDetails().getUsername(), projectId);
        context.put("project", XnatProjectdata.getXnatProjectdatasById(projectId, null, false));
    }

    public boolean allowGuestAccess() {
        return false;
    }
}
