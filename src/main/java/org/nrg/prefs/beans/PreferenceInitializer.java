package org.nrg.prefs.beans;

import java.util.Map;

public interface PreferenceInitializer {
    String getToolId();
    Map<String, Object> getPreferenceMap();
}
