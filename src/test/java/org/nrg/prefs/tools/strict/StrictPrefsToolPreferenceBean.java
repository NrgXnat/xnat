/*
 * prefs: org.nrg.prefs.tools.strict.StrictPrefsToolPreferenceBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tools.strict;

import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.List;

@NrgPreferenceBean(toolId = "strict",
                   toolName = "Strict Prefs Tool",
                   description = "This tests the strict mode on adding preferences")
@SuppressWarnings("WeakerAccess")
public class StrictPrefsToolPreferenceBean extends AbstractPreferenceBean {
    @Autowired
    public StrictPrefsToolPreferenceBean(final NrgPreferenceService preferenceService) {
        super(preferenceService);
    }

    @NrgPreference(defaultValue = "strictValueA")
    public String getStrictPrefA() {
        return getValue("strictPrefA");
    }

    public void setStrictPrefA(final String strictPrefA) throws InvalidPreferenceName {
        set(strictPrefA, "strictPrefA");
    }

    @NrgPreference(defaultValue = "strictValueB")
    public String getStrictPrefB() {
        return getValue("strictPrefB");
    }

    public void setStrictPrefB(final String strictPrefB) throws InvalidPreferenceName {
        set(strictPrefB, "strictPrefB");
    }

    public String getStrictPrefC() {
        return getValue("strictPrefC");
    }

    public void setStrictPrefC(final String strictPrefC) throws InvalidPreferenceName {
        set(strictPrefC, "strictPrefC");
    }
}
