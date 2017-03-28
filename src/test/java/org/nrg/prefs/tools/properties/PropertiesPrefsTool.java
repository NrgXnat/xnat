/*
 * prefs: org.nrg.prefs.tools.properties.PropertiesPrefsTool
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tools.properties;

import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PropertiesPrefsTool {
    public String getPropertyA() {
        return _preferences.getPropertyA();
    }

    public String getPropertyB() {
        return _preferences.getPropertyB();
    }

    public void setPropertyA(final String prefA) throws InvalidPreferenceName {
        _preferences.setPropertyA(prefA);
    }

    public void setPropertyB(final String prefB) throws InvalidPreferenceName {
        _preferences.setPropertyB(prefB);
    }

    public Map<String, Object> getPreferences() {
        return _preferences;
    }

    @Autowired
    private PropertiesTestToolPreferenceBean _preferences;
}
