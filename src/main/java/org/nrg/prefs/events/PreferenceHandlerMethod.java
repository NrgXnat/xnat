package org.nrg.prefs.events;

import java.util.List;
import java.util.Map;

public interface PreferenceHandlerMethod {
    List<String> getToolIds();
    List<String> getHandledPreferences();
    void handlePreferences(final Map<String, String> values);
    void handlePreference(final String preference, final String value);
}
