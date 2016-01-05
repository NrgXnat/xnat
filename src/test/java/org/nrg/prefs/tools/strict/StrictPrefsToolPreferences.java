package org.nrg.prefs.tools.strict;

import org.nrg.prefs.beans.BaseNrgPreferences;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPrefsService;

public class StrictPrefsToolPreferences extends BaseNrgPreferences {
    public StrictPrefsToolPreferences(final NrgPrefsService service, final String toolId) {
        super(service, toolId);
    }

    public String getStrictPrefA() {
        return getValue("strictPrefA");
    }

    public void setStrictPrefA(final String strictPrefA) throws InvalidPreferenceName {
        set("strictPrefA", strictPrefA);
    }

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
