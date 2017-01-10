/*
 * web: org.nrg.xnat.event.listeners.methods.PathHandlerMethod
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
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.utils.XnatUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class PathHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Autowired
    public PathHandlerMethod(final XnatUserProvider primaryAdminUserProvider, final XnatAppInfo appInfo) {
        super(primaryAdminUserProvider);
        _appInfo = appInfo;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateArchivePath();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (PREFERENCES.contains(preference)) {
            updateArchivePath();
        }
    }

    private void updateArchivePath() {
        if (!_appInfo.isInitialized()) {
            _log.warn("Application is not yet initialized, update archive path operation aborted.");
            return;
        }

        try {
            final UserI adminUser = getAdminUser();
            if (adminUser == null) {
                _log.error("No error occurred but failed to retrieve admin user, can't proceed with archive path update.");
            } else {
                ArcSpecManager.initialize(adminUser);
            }
        } catch (final ElementNotFoundException | UserInitException | NrgServiceRuntimeException e) {
            if (!(e instanceof NrgServiceRuntimeException) || ((NrgServiceRuntimeException) e).getServiceError() == NrgServiceError.UserServiceError) {
                _log.warn("The user for initializing the arcspec could not be initialized. This probably means the system is still initializing. Check the database if this is not the case.");
            } else {
                _log.error("An unknown error occurred trying to update the archive path.", e);
            }
        } catch (final Exception e) {
            _log.error("An unknown error occurred trying to update the archive path.", e);
        }
    }

    private static final Logger       _log        = LoggerFactory.getLogger(PathHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("archivePath", "prearchivePath", "cachePath", "ftpPath", "buildPath", "pipelinePath"));

    private final XnatAppInfo _appInfo;
}
