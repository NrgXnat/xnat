/*
 * prefs: org.nrg.prefs.tools.alias.AliasMigrationTestTool
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tools.alias;

import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AliasMigrationTestTool {
    @Autowired
    public AliasMigrationTestTool(final AliasMigrationTestToolPreferenceBean preferences) {
        _preferences = preferences;
    }

    public String getPrefA() {
        return _preferences.getPrefA();
    }

    public void setPrefA(final String prefA) throws InvalidPreferenceName {
        _preferences.setPrefA(prefA);
    }

    public String getPrefB() {
        return _preferences.getPrefB();
    }

    public void setPrefB(final String prefB) throws InvalidPreferenceName {
        _preferences.setPrefB(prefB);
    }

    public String getPrefC() {
        return _preferences.getPrefC();
    }

    public void setPrefC(final String prefC) throws InvalidPreferenceName {
        _preferences.setPrefC(prefC);
    }

    public Map<String, Object> getPreferences() {
        return _preferences;
    }

    private final AliasMigrationTestToolPreferenceBean _preferences;
}
