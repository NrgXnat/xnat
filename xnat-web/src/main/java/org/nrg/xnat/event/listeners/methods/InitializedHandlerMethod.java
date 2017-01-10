/*
 * web: org.nrg.xnat.event.listeners.methods.InitializedHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.utils.XnatUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class InitializedHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Autowired
    public InitializedHandlerMethod(final SiteConfigPreferences preferences, final XnatUserProvider primaryAdminUserProvider) {
        super(primaryAdminUserProvider);
        _preferences = preferences;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (values.containsKey(INITIALIZED)) {
            handlePreference(INITIALIZED, values.get(INITIALIZED));
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (PREFERENCES.contains(preference)) {
            try {
                initialize();
            } catch (InitializationException e) {
                throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred attempting to complete system initialization", e);
            }
        }
    }

    private void initialize() throws InitializationException {
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

        _log.info(buffer.toString());

        // In the case where the application hasn't yet been initialized, this operation should mean that the system is
        // being initialized from the set-up page. In that case, we need to propagate a few properties to the arc-spec
        // persistence to support
        try {
            ArcSpecManager.initialize(getAdminUser());
        } catch (Exception e) {
            throw new InitializationException(e);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(InitializedHandlerMethod.class);

    private static final String       INITIALIZED = "initialized";
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Collections.singletonList(INITIALIZED));

    private final SiteConfigPreferences _preferences;
}
