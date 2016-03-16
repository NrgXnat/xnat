package org.nrg.prefs.tools.basic;

import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Autowired
    private BasicTestToolPreferenceBean _preferences;
}
