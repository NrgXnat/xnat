package org.nrg.prefs.tools.basic;

import org.nrg.prefs.annotations.NrgPrefValue;
import org.nrg.prefs.annotations.NrgPrefsTool;
import org.nrg.prefs.exceptions.InvalidPreferenceName;

@NrgPrefsTool(toolId = "test",
        toolName = "Test Tool",
        description = "This is only a test.",
        preferencesClass = BasicTestToolPreferences.class,
        preferences = {@NrgPrefValue(name = "prefA", defaultValue = "valueA"), @NrgPrefValue(name = "prefB", defaultValue = "valueB")})
public class BasicTestTool {
    public BasicTestTool() {

    }

    @SuppressWarnings("unused")
    public void setPreferences(final BasicTestToolPreferences preferences) {
        _preferences = preferences;
    }

    public String getPrefA() {
        return _preferences.getTestToolPrefA();
    }

    public String getPrefB() {
        return _preferences.getTestToolPrefB();
    }

    public void setPrefA(final String prefA) throws InvalidPreferenceName {
        _preferences.setTestToolPrefA(prefA);
    }

    public void setPrefB(final String prefB) throws InvalidPreferenceName {
        _preferences.setTestToolPrefB(prefB);
    }

    private BasicTestToolPreferences _preferences;
}
