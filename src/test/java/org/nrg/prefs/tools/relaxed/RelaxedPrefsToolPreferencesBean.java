package org.nrg.prefs.tools.relaxed;

import org.nrg.prefs.annotations.NrgPreferencesBean;
import org.nrg.prefs.beans.AbstractPreferencesBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPrefsService;

@NrgPreferencesBean(toolId = "relaxed",
                    toolName = "Relaxed Prefs Tool",
                    description = "This tests the non-relaxed mode on adding preferences")
public class RelaxedPrefsToolPreferencesBean extends AbstractPreferencesBean {
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
