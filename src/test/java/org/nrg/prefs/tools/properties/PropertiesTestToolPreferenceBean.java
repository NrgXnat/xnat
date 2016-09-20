/*
 * org.nrg.prefs.tools.properties.PropertiesTestToolPreferenceBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tools.properties;

import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("WeakerAccess")
@NrgPreferenceBean(toolId = "properties", toolName = "Properties Test", description = "This is a test of the properties bean.")
public class PropertiesTestToolPreferenceBean extends AbstractPreferenceBean {
    @Autowired
    public PropertiesTestToolPreferenceBean(final NrgPreferenceService preferenceService) {
        super(preferenceService);
    }

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
