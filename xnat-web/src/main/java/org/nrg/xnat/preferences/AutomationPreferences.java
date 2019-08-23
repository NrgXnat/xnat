/*
 * web: org.nrg.xnat.preferences.AutomationPreferences
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.preferences;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xdat.preferences.EventTriggeringAbstractPreferenceBean;
import org.nrg.xdat.services.DataTypeAwareEventService;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("WeakerAccess")
@NrgPreferenceBean(toolId = AutomationPreferences.AUTOMATION_TOOL_ID,
                   toolName = "XNAT Automation Preferences",
                   description = "Manages preferences and settings for the XNAT automation services.",
                   properties = "META-INF/xnat/preferences/automation.properties",
                   strict = false)
@Slf4j
public class AutomationPreferences extends EventTriggeringAbstractPreferenceBean {
    public static final String AUTOMATION_TOOL_ID = "automation";

    @Autowired
    public AutomationPreferences(final NrgPreferenceService preferenceService, final DataTypeAwareEventService eventService, final ConfigPaths configPaths, final OrderedProperties initPrefs) {
        super(preferenceService, eventService, configPaths, initPrefs);
    }

    @NrgPreference(defaultValue = "true")
    public boolean isInternalScriptingEnabled() {
        return getBooleanValue("internalScriptingEnabled");
    }

    public void setInternalScriptingEnabled(final boolean internalScriptingEnabled) {
        try {
            setBooleanValue(internalScriptingEnabled, "internalScriptingEnabled");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name internalScriptingEnabled: something is very wrong here.", e);
        }
    }
}
