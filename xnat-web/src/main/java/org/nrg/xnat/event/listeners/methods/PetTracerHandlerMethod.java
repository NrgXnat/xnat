/*
 * web: org.nrg.xnat.event.listeners.methods.PetTracerHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.services.PETTracerUtils;
import org.nrg.xnat.utils.XnatUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class PetTracerHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Autowired
    public PetTracerHandlerMethod(final SiteConfigPreferences preferences, final XnatUserProvider primaryAdminUserProvider, final PETTracerUtils petTracerUtils) {
        super(primaryAdminUserProvider);
        _preferences = preferences;
        _petTracerUtils = petTracerUtils;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updatePetTracer();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (PREFERENCES.contains(preference)) {
            updatePetTracer();
        }
    }

    private void updatePetTracer() {
        try {
            _petTracerUtils.setSiteWideTracerList(getAdminUsername(), PETTracerUtils.buildScriptPath(PETTracerUtils.ResourceScope.SITE_WIDE, ""), _preferences.getSitewidePetTracers());
        } catch (Exception e) {
            _log.error("Failed to set sitewide anon script.", e);
        }
    }

    private static final Logger       _log        = LoggerFactory.getLogger(PetTracerHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Collections.singletonList("sitewidePetTracers"));

    private final SiteConfigPreferences _preferences;
    private final PETTracerUtils        _petTracerUtils;
}
