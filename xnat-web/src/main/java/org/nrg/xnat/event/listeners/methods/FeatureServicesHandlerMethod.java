/*
 * web: org.nrg.xnat.event.listeners.methods.FeatureServicesHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.security.helpers.Features;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class FeatureServicesHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateFeatureServices();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if(PREFERENCES.contains(preference)){
            updateFeatureServices();
        }
    }

	private void updateFeatureServices(){
        Features.setFeatureServiceToSiteConfigPreference();
        Features.setFeatureRepositoryServiceToSiteConfigPreference();
    }

    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("featureService", "featureRepositoryService"));

}
