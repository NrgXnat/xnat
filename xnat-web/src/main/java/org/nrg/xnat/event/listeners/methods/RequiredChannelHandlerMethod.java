/*
 * web: org.nrg.xnat.event.listeners.methods.RequiredChannelHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.security.TranslatingChannelProcessingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class RequiredChannelHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Autowired
    public RequiredChannelHandlerMethod(final SiteConfigPreferences preferences, final TranslatingChannelProcessingFilter filter) {
        _preferences = preferences;
        _filter = filter;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateRequiredChannel();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (PREFERENCES.contains(preference)) {
            updateRequiredChannel();
        }
    }

    private void updateRequiredChannel() {
        _filter.setRequiredChannel(_preferences.getSecurityChannel());
    }

    private static final List<String> PREFERENCES = ImmutableList.copyOf(Collections.singletonList("securityChannel"));

    private final SiteConfigPreferences              _preferences;
    private final TranslatingChannelProcessingFilter _filter;
}
