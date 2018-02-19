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

@Component
@Slf4j
public class InitializedHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    @Autowired
    public InitializedHandlerMethod(final XnatUserProvider primaryAdminUserProvider, final SiteConfigPreferences preferences) {
        super(primaryAdminUserProvider, "initialized");
        _preferences = preferences;
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        if (StringUtils.equals("initialized", preference)) {
            initialize();
        }
    }

    private void initialize() {
        final String adminEmail = _preferences.getAdminEmail();
        final String archivePath = _preferences.getArchivePath();
        final String buildPath = _preferences.getBuildPath();
        final String cachePath = _preferences.getCachePath();
        final boolean enableCsrfToken = _preferences.getEnableCsrfToken();
        final String ftpPath = _preferences.getFtpPath();
        final String pipelinePath = _preferences.getPipelinePath();
        final String prearchivePath = _preferences.getPrearchivePath();
        final boolean requireLogin = _preferences.getRequireLogin();
        final String siteId = _preferences.getSiteId();
        final String siteUrl = _preferences.getSiteUrl();
        final boolean userRegistration = _preferences.getUserRegistration();

        // TODO: We may actually need to put a null check in here and make this a Future that circles back once everything is properly initialized.
        final StringBuilder buffer = new StringBuilder("Preparing to complete system initialization with the final property settings of:").append(System.lineSeparator());
        buffer.append(" * adminEmail: ").append(adminEmail).append(System.lineSeparator());
        buffer.append(" * archivePath: ").append(archivePath).append(System.lineSeparator());
        buffer.append(" * buildPath: ").append(buildPath).append(System.lineSeparator());
        buffer.append(" * cachePath: ").append(cachePath).append(System.lineSeparator());
        buffer.append(" * enableCsrfToken: ").append(enableCsrfToken).append(System.lineSeparator());
        buffer.append(" * ftpPath: ").append(ftpPath).append(System.lineSeparator());
        buffer.append(" * pipelinePath: ").append(pipelinePath).append(System.lineSeparator());
        buffer.append(" * prearchivePath: ").append(prearchivePath).append(System.lineSeparator());
        buffer.append(" * requireLogin: ").append(requireLogin).append(System.lineSeparator());
        buffer.append(" * siteId: ").append(siteId).append(System.lineSeparator());
        buffer.append(" * siteUrl: ").append(siteUrl).append(System.lineSeparator());
        buffer.append(" * userRegistration: ").append(userRegistration).append(System.lineSeparator());

        log.info(buffer.toString());

        // In the case where the application hasn't yet been initialized, this operation should mean that the system is
        // being initialized from the set-up page. In that case, we need to propagate a few properties to the arc-spec
        // persistence to support
        try {
            ArcSpecManager.initialize(getAdminUser());
        } catch (Exception e) {
            throw new NrgServiceRuntimeException(new InitializationException(e));
        }
    }
    private final SiteConfigPreferences _preferences;
}
