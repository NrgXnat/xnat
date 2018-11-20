/*
 * core: org.nrg.xdat.turbine.modules.screens.AdminReport
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.util.RunData;

public abstract class AdminReport extends SecureReport {
    @Override
    protected boolean isAuthorized(RunData data) throws Exception {
        return isAuthorizedAdmin(data);
    }
}
