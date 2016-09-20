/*
 * org.nrg.prefs.events.PreferenceHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.events;

import java.util.List;
import java.util.Map;

public interface PreferenceHandlerMethod {
    List<String> getToolIds();
    List<String> getHandledPreferences();
    void handlePreferences(final Map<String, String> values);
    void handlePreference(final String preference, final String value);
}
