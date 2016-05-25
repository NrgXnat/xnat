package org.nrg.xnat.event.listeners.methods;

import org.nrg.prefs.events.PreferenceHandlerMethod;
import org.nrg.xdat.preferences.SiteConfigPreferences;

public abstract class AbstractSiteConfigPreferenceHandlerMethod implements PreferenceHandlerMethod {
    @Override
    public String getToolId() {
        return SiteConfigPreferences.SITE_CONFIG_TOOL_ID;
    }
}
