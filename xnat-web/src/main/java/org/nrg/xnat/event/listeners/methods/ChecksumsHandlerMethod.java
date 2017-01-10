/*
 * web: org.nrg.xnat.event.listeners.methods.ChecksumsHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.utils.CatalogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ChecksumsHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Autowired
    public ChecksumsHandlerMethod(final SiteConfigPreferences preferences) {
        _preferences = preferences;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateChecksums();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (PREFERENCES.contains(preference)) {
            updateChecksums();
        }
    }

    private void updateChecksums() {
        CatalogUtils.setChecksumConfiguration(_preferences.getChecksums());
    }

    private static final List<String> PREFERENCES = ImmutableList.copyOf(Collections.singletonList("checksums"));

    private final SiteConfigPreferences _preferences;
}
