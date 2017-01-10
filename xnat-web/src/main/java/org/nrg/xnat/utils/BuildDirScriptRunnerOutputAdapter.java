/*
 * web: org.nrg.xnat.utils.BuildDirScriptRunnerOutputAdapter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

import org.nrg.automation.entities.Script;
import org.nrg.automation.runners.PathBasedScriptRunnerOutputAdapter;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

public class BuildDirScriptRunnerOutputAdapter extends PathBasedScriptRunnerOutputAdapter {
    public BuildDirScriptRunnerOutputAdapter() throws NrgServiceException {
    }

    @Override
    public PrintWriter getWriter(final Script script) {
        if (!hasPath()) {
            initialize(ArcSpecManager.GetInstance() != null ? ArcSpecManager.GetInstance().getGlobalBuildPath() : null);
            if (ArcSpecManager.GetInstance() == null) {
                _log.warn("The arc-spec manager was null, meaning that the build dir script runner output adapter has not been initialized with a path.");
            } else if (_log.isDebugEnabled()) {
                _log.debug("The build dir script runner output adapter was initialized with the path: " + ArcSpecManager.GetInstance().getGlobalBuildPath());
            }
        }
        return super.getWriter(script);
    }

    private static final Logger _log = LoggerFactory.getLogger(BuildDirScriptRunnerOutputAdapter.class);
}
