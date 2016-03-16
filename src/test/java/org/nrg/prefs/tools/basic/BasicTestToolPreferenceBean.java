package org.nrg.prefs.tools.basic;

import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;

@NrgPreferenceBean(toolId = "basic", toolName = "Basic Test", description = "This is only a test.", strict = true)
public class BasicTestToolPreferenceBean extends AbstractPreferenceBean {
    @NrgPreference(defaultValue = "valueA")
    public String getPrefA() {
        return getValue("prefA");
    }

    public void setPrefA(final String prefA) throws InvalidPreferenceName {
        set(prefA);
    }

    @NrgPreference(defaultValue = "valueB")
    public String getPrefB() {
        return getValue("prefB");
    }

    public void setPrefB(final String prefB) throws InvalidPreferenceName {
        set(prefB);
    }
}
