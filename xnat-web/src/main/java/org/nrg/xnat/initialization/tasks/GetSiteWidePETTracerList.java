/*
 * web: org.nrg.xnat.initialization.tasks.GetSiteWidePETTracerList
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xnat.services.PETTracerUtils;
import org.nrg.xnat.utils.XnatUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GetSiteWidePETTracerList extends AbstractInitializingTask {
    @Autowired
    public GetSiteWidePETTracerList(final XnatUserProvider primaryAdminUserProvider, final PETTracerUtils petTracerUtils) {
        _adminUsername = primaryAdminUserProvider.getLogin();
        _petTracerUtils = petTracerUtils;
    }

    @Override
    public String getTaskName() {
        return "Get site-wide PET tracer list";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        try {
            final String path = PETTracerUtils.buildScriptPath(PETTracerUtils.ResourceScope.SITE_WIDE, "");
            final Configuration configuration = _petTracerUtils.getTracerList(path, null);
            if (configuration == null) {
                _log.info("Creating PET Tracer List.");
                final String siteWide = PETTracerUtils.getDefaultTracerList();
                _petTracerUtils.setSiteWideTracerList(_adminUsername, path, siteWide);
            }
        } catch (ConfigServiceException e) {
            throw new InitializingTaskException(InitializingTaskException.Level.Warn, "An error occurred accessing the configuration service, it may not be initialized yet.", e);
        } catch (IOException e) {
            throw new InitializingTaskException(InitializingTaskException.Level.Error, "Unable to either find or retrieve the PET tracer list.", e);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(GetSiteWidePETTracerList.class);

    private final String         _adminUsername;
    private final PETTracerUtils _petTracerUtils;
}
