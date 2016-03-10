package org.nrg.prefs.tools.basic;

import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferencesBean;
import org.nrg.prefs.beans.AbstractPreferencesBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPrefsService;

@NrgPreferencesBean(toolId = "basic", toolName = "Basic Test", description = "This is only a test.")
public class BasicTestToolPreferencesBean extends AbstractPreferencesBean {
    @NrgPreference(defaultValue = "prefA")
    public String getPrefA() {
        return getValue("prefA");
    }

    public void setPrefA(final String prefA) throws InvalidPreferenceName {
        set("prefA", prefA);
    }

    @NrgPreference(defaultValue = "prefB")
    public String getPrefB() {
        return getValue("prefB");
    }

    public void setPrefB(final String prefB) throws InvalidPreferenceName {
        set("prefB", prefB);
    }
}
