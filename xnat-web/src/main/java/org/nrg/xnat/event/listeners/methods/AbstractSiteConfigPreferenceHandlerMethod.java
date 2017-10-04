/*
 * web: org.nrg.xnat.event.listeners.methods.AbstractSiteConfigPreferenceHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import org.nrg.prefs.events.AbstractPreferenceHandlerMethod;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.utils.XnatUserProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractSiteConfigPreferenceHandlerMethod extends AbstractPreferenceHandlerMethod {
    protected AbstractSiteConfigPreferenceHandlerMethod() {
        _userProvider = null;
    }

    protected AbstractSiteConfigPreferenceHandlerMethod(final XnatUserProvider userProvider) {
        _userProvider = userProvider;
    }

    @Override
    public abstract List<String> getHandledPreferences();

    @Override
    public abstract void handlePreferences(final Map<String, String> values);

    @Override
    public abstract void handlePreference(final String preference, final String value);

    @Override
    public List<String> getToolIds() {
        return Collections.singletonList(SiteConfigPreferences.SITE_CONFIG_TOOL_ID);
    }

    protected UserI getAdminUser() {
        return _userProvider != null ? _userProvider.get() : null;
    }

    protected String getAdminUsername() {
        return _userProvider != null ? _userProvider.getLogin() : "";
    }

    private final XnatUserProvider _userProvider;
}
