package org.nrg.prefs.tools.relaxed;

import org.nrg.prefs.annotations.NrgPrefValue;
import org.nrg.prefs.annotations.NrgPrefsTool;
import org.nrg.prefs.exceptions.InvalidPreferenceName;

@NrgPrefsTool(toolId = "relaxed",
        toolName = "Relaxed Prefs Tool",
        description = "This tests the non-relaxed mode on adding preferences",
        preferencesClass = RelaxedPrefsToolPreferences.class,
        preferences = {@NrgPrefValue(name = "relaxedPrefA", defaultValue = "defaultA"), @NrgPrefValue(name = "relaxedPrefB", defaultValue = "defaultB")})
public class RelaxedPrefsTool {
    @SuppressWarnings("unused")
    public void setPreferences(final RelaxedPrefsToolPreferences preferences) {
        _preferences = preferences;
    }

    public String getRelaxedPrefA() {
        return _preferences.getRelaxedPrefA();
    }

    public void setRelaxedPrefA(final String relaxedPrefA) throws InvalidPreferenceName {
        _preferences.setRelaxedPrefA(relaxedPrefA);
    }

    public String getRelaxedPrefB() {
        return _preferences.getRelaxedPrefB();
    }

    public void setRelaxedPrefB(final String relaxedPrefB) throws InvalidPreferenceName {
        _preferences.setRelaxedPrefB(relaxedPrefB);
    }

    public String getRelaxedPrefC() {
        return _preferences.getRelaxedPrefC();
    }

    public void setRelaxedPrefC(final String relaxedPrefC) throws InvalidPreferenceName {
        _preferences.setRelaxedPrefC(relaxedPrefC);
    }

    private RelaxedPrefsToolPreferences _preferences;
}
