/*
 * web: org.nrg.xnat.event.listeners.methods.PetTracerHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.services.PETTracerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PetTracerHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    @Autowired
    public PetTracerHandlerMethod(final XnatUserProvider primaryAdminUserProvider, final PETTracerUtils petTracerUtils) {
        super(primaryAdminUserProvider, "sitewidePetTracers");
        _petTracerUtils = petTracerUtils;
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        try {
            _petTracerUtils.setSiteWideTracerList(getAdminUsername(), value);
        } catch (Exception e) {
            log.error("Failed to set sitewide anon script.", e);
        }
    }

    private final PETTracerUtils _petTracerUtils;
}
