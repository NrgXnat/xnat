/*
 * prefs: org.nrg.prefs.tools.basic.BasicTestTool
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tools.basic;

import org.nrg.prefs.beans.PreferenceBean;
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

    public BasicEnum getPrefC() {
        return _preferences.getPrefC();
    }

    public void setPrefA(final String prefA) throws InvalidPreferenceName {
        _preferences.setPrefA(prefA);
    }

    public void setPrefB(final String prefB) throws InvalidPreferenceName {
        _preferences.setPrefB(prefB);
    }

    public void setPrefC(final BasicEnum prefC) throws InvalidPreferenceName {
        _preferences.setPrefC(prefC);
    }

    public Map<String, Object> getPreferences() {
        return _preferences;
    }

    public PreferenceBean getPreferenceBean() {
        return _preferences;
    }

    @Autowired
    private BasicTestToolPreferenceBean _preferences;
}
