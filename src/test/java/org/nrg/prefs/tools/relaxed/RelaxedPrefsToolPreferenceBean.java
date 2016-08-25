package org.nrg.prefs.tools.relaxed;

import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.List;

@NrgPreferenceBean(toolId = "relaxed",
                   toolName = "Relaxed Prefs Tool",
                   description = "This tests the non-relaxed mode on adding preferences",
                   strict = false)
public class RelaxedPrefsToolPreferenceBean extends AbstractPreferenceBean {
    @Autowired
    public RelaxedPrefsToolPreferenceBean(final NrgPreferenceService preferenceService) {
        super(preferenceService);
    }

    public String getRelaxedPrefA() {
        return getValue("relaxedPrefA");
    }

    public void setRelaxedPrefA(final String relaxedPrefA) throws InvalidPreferenceName {
        set(relaxedPrefA, "relaxedPrefA");
    }

    public String getRelaxedPrefB() {
        return getValue("relaxedPrefB");
    }

    public void setRelaxedPrefB(final String relaxedPrefB) throws InvalidPreferenceName {
        set(relaxedPrefB, "relaxedPrefB");
    }

    public String getRelaxedPrefC() {
        return getValue("relaxedPrefC");
    }

    public void setRelaxedPrefC(final String relaxedPrefC) throws InvalidPreferenceName {
        set(relaxedPrefC, "relaxedPrefC");
    }
}
