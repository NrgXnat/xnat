package org.nrg.prefs.tools.basic;

import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("WeakerAccess")
@NrgPreferenceBean(toolId = "basic", toolName = "Basic Test", description = "This is only a test.")
public class BasicTestToolPreferenceBean extends AbstractPreferenceBean {
    @Autowired
    public BasicTestToolPreferenceBean(final NrgPreferenceService preferenceService) {
        super(preferenceService);
    }

    @NrgPreference(defaultValue = "valueA")
    public String getPrefA() {
        return getValue("prefA");
    }

    public void setPrefA(final String prefA) throws InvalidPreferenceName {
        set(prefA, "prefA");
    }

    @NrgPreference(defaultValue = "valueB")
    public String getPrefB() {
        return getValue("prefB");
    }

    public void setPrefB(final String prefB) throws InvalidPreferenceName {
        set(prefB, "prefB");
    }
}
