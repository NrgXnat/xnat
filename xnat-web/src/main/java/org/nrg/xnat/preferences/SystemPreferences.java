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
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.services.NrgPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;

@NrgPreferenceBean(toolId = SystemPreferences.AUTOMATION_TOOL_ID,
                   toolName = "XNAT System Preferences",
                   description = "Manages preferences that should not be surfaced to users.")
@Slf4j
public class SystemPreferences extends AbstractPreferenceBean {
    public static final String AUTOMATION_TOOL_ID = "system";

    @Autowired
    public SystemPreferences(final NrgPreferenceService preferenceService,
                             final ConfigPaths configPaths,
                             final OrderedProperties initPrefs) {
        super(preferenceService, configPaths, initPrefs);
    }

    @NrgPreference
    public String getDefaultAdminPassword() {
        return getValue("defaultAdminPassword");
    }
}
