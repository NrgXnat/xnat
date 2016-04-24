package org.nrg.prefs.tools.properties;

import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;

@SuppressWarnings("WeakerAccess")
@NrgPreferenceBean(toolId = "properties", toolName = "Properties Test", description = "This is a test of the properties bean.")
public class PropertiesTestToolPreferenceBean extends AbstractPreferenceBean {
    @NrgPreference(defaultValue = "valueA", property = "property.A")
    public String getPropertyA() {
        return getValue("property.A");
    }

    public void setPropertyA(final String propertyA) throws InvalidPreferenceName {
        set(propertyA, "property.A");
    }

    @NrgPreference(defaultValue = "valueB", property = "property.B")
    public String getPropertyB() {
        return getValue("property.B");
    }

    public void setPropertyB(final String propertyB) throws InvalidPreferenceName {
        set(propertyB, "property.B");
    }
}
