package org.nrg.prefs.tools.basic;

import org.nrg.prefs.beans.BaseNrgPreferences;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPrefsService;

public class BasicTestToolPreferences extends BaseNrgPreferences {
    public BasicTestToolPreferences(final NrgPrefsService service, final String toolId) {
        super(service, toolId);
    }

    public String getTestToolPrefA() {
        return getValue("prefA");
    }

    public void setTestToolPrefA(final String prefA) throws InvalidPreferenceName {
        set("prefA", prefA);
    }

    public String getTestToolPrefB() {
        return getValue("prefB");
    }

    public void setTestToolPrefB(final String prefB) throws InvalidPreferenceName {
        set("prefB", prefB);
    }
}
