/*
 * web: org.nrg.xnat.event.listeners.methods.AbstractSiteConfigNotificationsPreferenceHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import org.nrg.prefs.events.AbstractPreferenceHandlerMethod;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.SiteConfigPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractSiteConfigNotificationsPreferenceHandlerMethod extends AbstractPreferenceHandlerMethod {
    @Override
    public List<String> getToolIds() {
        return new ArrayList<>(Arrays.asList(NotificationsPreferences.NOTIFICATIONS_TOOL_ID, SiteConfigPreferences.SITE_CONFIG_TOOL_ID));
    }
}
