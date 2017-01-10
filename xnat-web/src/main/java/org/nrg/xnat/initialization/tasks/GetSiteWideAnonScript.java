/*
 * web: org.nrg.xnat.initialization.tasks.GetSiteWideAnonScript
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import org.nrg.config.entities.Configuration;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.helpers.merge.AnonUtils;
import org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;

@Component
public class GetSiteWideAnonScript extends AbstractInitializingTask {
    @Autowired
    public GetSiteWideAnonScript(final SiteConfigPreferences preferences, final AnonUtils anonUtils) {
        super();
        _preferences = preferences;
        _anonUtils = anonUtils;
    }

    @Override
    public String getTaskName() {
        return "Get site-wide anon script";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        try {
            final Configuration initConfig = _anonUtils.getSiteWideScriptConfiguration();
            if (initConfig == null) {
                _log.info("Creating Script Table.");
                final String siteWideScript = DefaultAnonUtils.getDefaultScript();
                final String adminUser = _preferences.getReceivedFileUser();
                if (adminUser != null) {
                    _anonUtils.setSiteWideScript(adminUser, siteWideScript);
                    _preferences.setSitewideAnonymizationScript(siteWideScript);
                } else {
                    throw new InitializingTaskException(InitializingTaskException.Level.Error, "Site administrator not found.");
                }
            }
            // there is a default site-wide script, so nothing to do here for the else.
        } catch (FileNotFoundException e) {
            throw new InitializingTaskException(InitializingTaskException.Level.SingleNotice, "Couldn't find default anonymization script, will try again.", e);
        } catch (Exception e) {
            throw new InitializingTaskException(InitializingTaskException.Level.Error, "Unable to either find or initialize script database", e);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(GetSiteWideAnonScript.class);

    private final SiteConfigPreferences _preferences;
    private final AnonUtils             _anonUtils;
}
