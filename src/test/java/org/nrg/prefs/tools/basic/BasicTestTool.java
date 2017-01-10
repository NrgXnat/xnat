/*
 * prefs: org.nrg.prefs.tools.basic.BasicTestTool
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tools.basic;

import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BasicTestTool {
    public String getPrefA() {
        return _preferences.getPrefA();
    }

    public String getPrefB() {
        return _preferences.getPrefB();
    }

    public void setPrefA(final String prefA) throws InvalidPreferenceName {
        _preferences.setPrefA(prefA);
    }

    public void setPrefB(final String prefB) throws InvalidPreferenceName {
        _preferences.setPrefB(prefB);
    }

    public Map<String, Object> getPreferenceMap() {
        return _preferences.getPreferenceMap();
    }

    @Autowired
    private BasicTestToolPreferenceBean _preferences;
}
