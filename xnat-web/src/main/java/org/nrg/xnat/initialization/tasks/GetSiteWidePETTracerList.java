/*
 * web: org.nrg.xnat.initialization.tasks.GetSiteWidePETTracerList
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import lombok.extern.slf4j.Slf4j;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.services.PETTracerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class GetSiteWidePETTracerList extends AbstractInitializingTask {
    @Autowired
    public GetSiteWidePETTracerList(final XnatUserProvider primaryAdminUserProvider, final PETTracerUtils petTracerUtils, final SiteConfigPreferences preferences) {
        _adminUsername = primaryAdminUserProvider.getLogin();
        _petTracerUtils = petTracerUtils;
        _preferences = preferences;
    }

    @Override
    public String getTaskName() {
        return "Get site-wide PET tracer list";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        try {
            final Configuration configuration = _petTracerUtils.getSiteWideTracerList();
            if (configuration == null) {
                final String tracers = PETTracerUtils.getDefaultTracerList();
                log.info("No site-wide PET tracer setting found, storing default PET tracer list: {}", tracers);
                _petTracerUtils.setSiteWideTracerList(_adminUsername, tracers);
                _preferences.setSitewidePetTracers(tracers);
            }
        } catch (ConfigServiceException e) {
            throw new InitializingTaskException(InitializingTaskException.Level.Warn, "An error occurred accessing the configuration service, it may not be initialized yet.", e);
        } catch (IOException e) {
            throw new InitializingTaskException(InitializingTaskException.Level.Error, "Unable to either find or retrieve the PET tracer list.", e);
        }
    }

    private final String                _adminUsername;
    private final PETTracerUtils        _petTracerUtils;
    private final SiteConfigPreferences _preferences;
}
