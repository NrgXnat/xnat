package org.nrg.xnat.event.listeners.methods;

import org.nrg.prefs.events.PreferenceHandlerMethod;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.SiteConfigPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractSiteConfigNotificationsPreferenceHandlerMethod implements PreferenceHandlerMethod {
    @Override
    public List<String> getToolIds() {
        return new ArrayList<String>(Arrays.asList(NotificationsPreferences.NOTIFICATIONS_TOOL_ID, SiteConfigPreferences.SITE_CONFIG_TOOL_ID));
    }
}
