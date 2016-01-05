package org.nrg.prefs.tools.relaxed;

import org.nrg.prefs.beans.BaseNrgPreferences;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPrefsService;

public class RelaxedPrefsToolPreferences extends BaseNrgPreferences {
    public RelaxedPrefsToolPreferences(final NrgPrefsService service, final String toolId) {
        super(service, toolId);
    }

    public String getRelaxedPrefA() {
        return getValue("relaxedPrefA");
    }

    public void setRelaxedPrefA(final String relaxedPrefA) throws InvalidPreferenceName {
        set("relaxedPrefA", relaxedPrefA);
    }

    public String getRelaxedPrefB() {
        return getValue("relaxedPrefB");
    }

    public void setRelaxedPrefB(final String relaxedPrefB) throws InvalidPreferenceName {
        set("relaxedPrefB", relaxedPrefB);
    }

    public String getRelaxedPrefC() {
        return getValue("relaxedPrefC");
    }

    public void setRelaxedPrefC(final String relaxedPrefC) throws InvalidPreferenceName {
        set("relaxedPrefC", relaxedPrefC);
    }
}
