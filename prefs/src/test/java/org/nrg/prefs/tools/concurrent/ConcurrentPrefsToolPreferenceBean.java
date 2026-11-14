/*
 * prefs: org.nrg.prefs.tools.concurrent.ConcurrentPrefsToolPreferenceBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tools.concurrent;

import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A non-strict preference bean for testing concurrent reads, writes and invalidations.
 */
@NrgPreferenceBean(toolId = "concurrent", toolName = "Concurrency Test", description = "Tests concurrent access to a preference bean.", strict = false)
public class ConcurrentPrefsToolPreferenceBean extends AbstractPreferenceBean {
    @Autowired
    public ConcurrentPrefsToolPreferenceBean(final NrgPreferenceService preferenceService) {
        super(preferenceService);
    }

    @NrgPreference(defaultValue = "slowInitial")
    public String getSlowPref() {
        final String   value = getValue("slowPref");
        final Runnable hook  = _afterSlowPrefRead;
        if (hook != null) {
            hook.run();
        }
        return value;
    }

    public void setSlowPref(final String slowPref) throws InvalidPreferenceName {
        set(slowPref, "slowPref");
    }

    @NrgPreference(defaultValue = "plainInitial")
    public String getPlainPref() {
        return getValue("plainPref");
    }

    public void setPlainPref(final String plainPref) throws InvalidPreferenceName {
        set(plainPref, "plainPref");
    }

    @NrgPreference(defaultValue = "derived")
    public String getDerivedPref() {
        return getValue("derivedPref") + "+" + get("plainPref");
    }

    /**
     * Runs the hook in {@link #getSlowPref()} after it reads the stored value and before it returns it.
     *
     * @param hook The hook to run, or null to run none.
     */
    public void setAfterSlowPrefRead(final Runnable hook) {
        _afterSlowPrefRead = hook;
    }

    private volatile Runnable _afterSlowPrefRead;
}
