package org.nrg.prefs.tools.strict;

import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StrictPrefsTool {
    public String getStrictPrefA() {
        return _preferences.getStrictPrefA();
    }

    public void setStrictPrefA(final String strictPrefA) throws InvalidPreferenceName {
        _preferences.setStrictPrefA(strictPrefA);
    }

    public String getStrictPrefB() {
        return _preferences.getStrictPrefB();
    }

    public void setStrictPrefB(final String strictPrefB) throws InvalidPreferenceName {
        _preferences.setStrictPrefB(strictPrefB);
    }

    public String getStrictPrefC() {
        return _preferences.getStrictPrefC();
    }

    public void setStrictPrefC(final String strictPrefC) throws InvalidPreferenceName {
        _preferences.setStrictPrefC(strictPrefC);
    }

    @Autowired
    private StrictPrefsToolPreferenceBean _preferences;
}
