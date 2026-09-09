/*
 * core: org.nrg.xdat.turbine.modules.screens.AdminScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;

public abstract class AdminScreen extends SecureScreen {
    @Override
    protected boolean isAuthorized(final PipelineData pipelineData) throws Exception {
        RunData data = pipelineData.getRunData();
        return isAuthorizedAdmin(data);
    }
}
