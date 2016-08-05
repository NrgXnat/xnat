package org.nrg.xnat.event.listeners.methods;

import org.nrg.prefs.events.PreferenceHandlerMethod;
import org.nrg.xdat.preferences.NotificationsPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractNotificationsPreferenceHandlerMethod implements PreferenceHandlerMethod {
    @Override
    public List<String> getToolIds() {
        return new ArrayList<>(Collections.singletonList(NotificationsPreferences.NOTIFICATIONS_TOOL_ID));
    }
}
