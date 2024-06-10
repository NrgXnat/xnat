/*
 * web: org.nrg.xnat.event.listeners.methods.InitializedHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.nrg.xdat.preferences.SiteConfigPreferences.INITIALIZED;

@Component
@Slf4j
public class InitializedHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    @Autowired
    public InitializedHandlerMethod(final XnatUserProvider primaryAdminUserProvider, final SiteConfigPreferences preferences) {
        super(primaryAdminUserProvider, INITIALIZED);
        _preferences = preferences;
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        if (StringUtils.equals(INITIALIZED, preference)) {
            initialize();
        }
    }

    private void initialize() {
        // TODO: We may actually need to put a null check in here and make this a Future that circles back once everything is properly initialized.
        log.info(LOG_FORMAT, _preferences.getAdminEmail(), _preferences.getArchivePath(), _preferences.getBuildPath(), _preferences.getCachePath(), _preferences.getEnableCsrfToken(), _preferences.getFtpPath(), _preferences.getPipelinePath(), _preferences.getPrearchivePath(), _preferences.getRequireLogin(), _preferences.getSiteId(), _preferences.getSiteUrl(), _preferences.getUserRegistration());

        // In the case where the application hasn't yet been initialized, this operation should mean that the system is
        // being initialized from the set-up page. In that case, we need to propagate a few properties to the arc-spec
        // persistence to support
        try {
            ArcSpecManager.initialize(getAdminUser());
        } catch (Exception e) {
            throw new NrgServiceRuntimeException(new InitializationException(e));
        }
    }

    private static final String LOG_FORMAT = "Preparing to complete system initialization with the final property settings of:\n* adminEmail: {}\\n * archivePath: {}\\n * buildPath: {}\\n * cachePath: {}\\n * enableCsrfToken: {}\\n * ftpPath: {}\\n * pipelinePath: {}\\n * prearchivePath: {}\\n * requireLogin: {}\\n * siteId: {}\\n * siteUrl: {}\\n * userRegistration: {}";

    private final SiteConfigPreferences _preferences;
}
