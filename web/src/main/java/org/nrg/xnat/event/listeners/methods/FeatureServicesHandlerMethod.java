/*
 * web: org.nrg.xnat.event.listeners.methods.FeatureServicesHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.security.helpers.Features;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FeatureServicesHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    @Autowired
    public FeatureServicesHandlerMethod() {
        super(FEATURE_SERVICE, FEATURE_REPOSITORY_SERVICE);
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        switch (preference) {
            case FEATURE_SERVICE:
                Features.setFeatureServiceImplementation(value);
                break;

            case FEATURE_REPOSITORY_SERVICE:
                Features.setFeatureRepositoryServiceImplementation(value);
                break;
        }
    }

    private static final String FEATURE_SERVICE            = "featureService";
    private static final String FEATURE_REPOSITORY_SERVICE = "featureRepositoryService";
}
