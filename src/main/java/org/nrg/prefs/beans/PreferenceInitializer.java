/*
 * prefs: org.nrg.prefs.beans.PreferenceInitializer
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.beans;

import java.util.Map;

public interface PreferenceInitializer {
    String getToolId();
    Map<String, Object> getPreferenceMap();
}
