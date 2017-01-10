/*
 * web: org.nrg.xnat.event.listeners.methods.AnonymizationHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.helpers.merge.AnonUtils;
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
public class AnonymizationHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Autowired
    public AnonymizationHandlerMethod(final SiteConfigPreferences preferences, final XnatUserProvider primaryAdminUserProvider, final AnonUtils anonUtils) {
        super(primaryAdminUserProvider);
        _preferences = preferences;
        _anonUtils = anonUtils;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateAnon();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (PREFERENCES.contains(preference)) {
            updateAnon();
        }
    }

    private void updateAnon() {
        try {
            _anonUtils.setSiteWideScript(getAdminUsername(), _preferences.getSitewideAnonymizationScript());
        } catch (Exception e) {
            _log.error("Failed to set sitewide anon script.", e);
        }
        try {
            if (_preferences.getEnableSitewideAnonymizationScript()) {
                _anonUtils.enableSiteWide(getAdminUsername());
            } else {
                _anonUtils.disableSiteWide(getAdminUsername());
            }
        } catch (Exception e) {
            _log.error("Failed to enable/disable sitewide anon script.", e);
        }
    }

    private static final Logger       _log        = LoggerFactory.getLogger(AnonymizationHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("enableSitewideAnonymizationScript", "sitewideAnonymizationScript"));

    private final SiteConfigPreferences _preferences;
    private final AnonUtils             _anonUtils;
}
