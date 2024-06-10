/*
 * web: org.nrg.xnat.event.listeners.methods.PathHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PathHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    @Autowired
    public PathHandlerMethod(final XnatUserProvider primaryAdminUserProvider, final XnatAppInfo appInfo) {
        super(primaryAdminUserProvider, "archivePath", "prearchivePath", "cachePath", "ftpPath", "buildPath", "pipelinePath");
        _appInfo = appInfo;
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        if (!_appInfo.isInitialized()) {
            log.warn("Application is not yet initialized, update archive path operation aborted.");
            return;
        }

        try {
            final UserI adminUser = getAdminUser();
            if (adminUser == null) {
                log.error("No error occurred but failed to retrieve admin user, can't proceed with archive path update.");
            } else {
                ArcSpecManager.initialize(adminUser);
            }
        } catch (final ElementNotFoundException | UserInitException | NrgServiceRuntimeException e) {
            if (!(e instanceof NrgServiceRuntimeException) || ((NrgServiceRuntimeException) e).getServiceError() == NrgServiceError.UserServiceError) {
                log.warn("The user for initializing the arcspec could not be initialized. This probably means the system is still initializing. Check the database if this is not the case.");
            } else {
                log.error("An unknown error occurred trying to update the archive path.", e);
            }
        } catch (final Exception e) {
            log.error("An unknown error occurred trying to update the archive path.", e);
        }
    }

    private final XnatAppInfo _appInfo;
}
