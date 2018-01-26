/*
 * web: org.nrg.xnat.event.listeners.methods.AnonymizationHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.helpers.merge.AnonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AnonymizationHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    @Autowired
    public AnonymizationHandlerMethod(final XnatUserProvider primaryAdminUserProvider, final AnonUtils anonUtils) {
        super(primaryAdminUserProvider, ENABLE_SITEWIDE_SCRIPT, SITEWIDE_ANONYMIZATION_SCRIPT);
        _anonUtils = anonUtils;
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        switch (preference) {
            case ENABLE_SITEWIDE_SCRIPT:
                if (Boolean.parseBoolean(value)) {
                    try {
                        _anonUtils.enableSiteWide(getAdminUsername());
                    } catch (Exception e) {
                        log.error("Failed to enable sitewide anon script.", e);
                    }
                } else {
                    try {
                        _anonUtils.disableSiteWide(getAdminUsername());
                    } catch (Exception e) {
                        log.error("Failed to disable sitewide anon script.", e);
                    }
                }
                break;

            case SITEWIDE_ANONYMIZATION_SCRIPT:
                try {
                    _anonUtils.setSiteWideScript(getAdminUsername(), value);
                } catch (Exception e) {
                    log.error("Failed to set sitewide anon script.", e);
                }
                break;
        }
    }

    private static final String ENABLE_SITEWIDE_SCRIPT        = "enableSitewideAnonymizationScript";
    private static final String SITEWIDE_ANONYMIZATION_SCRIPT = "sitewideAnonymizationScript";

    private final AnonUtils _anonUtils;
}
