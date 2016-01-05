package org.nrg.prefs.tools.strict;

import org.nrg.prefs.annotations.NrgPrefValue;
import org.nrg.prefs.annotations.NrgPrefsTool;
import org.nrg.prefs.exceptions.InvalidPreferenceName;

@NrgPrefsTool(toolId = "strict",
        toolName = "Strict Prefs Tool",
        description = "This tests the strict mode on adding preferences",
        preferencesClass = StrictPrefsToolPreferences.class,
        preferences = {@NrgPrefValue(name = "strictPrefA", defaultValue = "defaultA"), @NrgPrefValue(name = "strictPrefB", defaultValue = "defaultB")},
        strict = true)
public class StrictPrefsTool {
    @SuppressWarnings("unused")
    public void setPreferences(final StrictPrefsToolPreferences preferences) {
        _preferences = preferences;
    }

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

    private StrictPrefsToolPreferences _preferences;
}
