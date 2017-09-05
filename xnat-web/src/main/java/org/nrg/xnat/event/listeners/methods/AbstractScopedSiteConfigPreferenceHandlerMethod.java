/*
 * web: org.nrg.xnat.event.listeners.methods.AbstractSiteConfigPreferenceHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import org.nrg.xnat.utils.XnatUserProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractScopedSiteConfigPreferenceHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    protected AbstractScopedSiteConfigPreferenceHandlerMethod(final String... preferences) {
        this(null, Arrays.asList(preferences));
    }

    protected AbstractScopedSiteConfigPreferenceHandlerMethod(final List<String> preferences) {
        this(null, preferences);
    }

    protected AbstractScopedSiteConfigPreferenceHandlerMethod(final XnatUserProvider userProvider, final String... preferences) {
        this(userProvider, Arrays.asList(preferences));
    }

    protected AbstractScopedSiteConfigPreferenceHandlerMethod(final XnatUserProvider userProvider, final List<String> preferences) {
        super(userProvider);
        _preferences = preferences;
    }

    @Override
    public List<String> getHandledPreferences() {
        return _preferences;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        final Set<String> keys = values.keySet();
        keys.retainAll(_preferences);
        for (final String preference : keys) {
            handlePreference(preference, values.get(preference));
        }
    }

    @Override
    public abstract void handlePreference(final String preference, final String value);

    private final List<String> _preferences;
}
