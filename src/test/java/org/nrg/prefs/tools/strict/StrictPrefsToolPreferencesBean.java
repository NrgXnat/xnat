package org.nrg.prefs.tools.strict;

import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferencesBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;

@NrgPreferenceBean(toolId = "strict",
                   toolName = "Strict Prefs Tool",
                   description = "This tests the strict mode on adding preferences")
public class StrictPrefsToolPreferencesBean extends AbstractPreferencesBean {
    @NrgPreference(defaultValue = "strictValueA")
    public String getStrictPrefA() {
        return getValue("strictPrefA");
    }

    public void setStrictPrefA(final String strictPrefA) throws InvalidPreferenceName {
        set("strictPrefA", strictPrefA);
    }

    @NrgPreference(defaultValue = "strictValueB")
    public String getStrictPrefB() {
        return getValue("strictPrefB");
    }

    public void setStrictPrefB(final String strictPrefB) throws InvalidPreferenceName {
        set("strictPrefB", strictPrefB);
    }

    public String getStrictPrefC() {
        return getValue("strictPrefC");
    }

    public void setStrictPrefC(final String strictPrefC) throws InvalidPreferenceName {
        set("strictPrefC", strictPrefC);
    }
}
