/*
 * spawner: org.nrg.xnat.spawner.preferences.SpawnerPreferences
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.preferences;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XnatMixIn;
import org.nrg.framework.beans.ProxiedBeanMixIn;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xdat.preferences.EventTriggeringAbstractPreferenceBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;

@NrgPreferenceBean(toolId = SpawnerPreferences.SPAWNER_TOOL_ID,
                   toolName = "XNAT Spawner Preferences",
                   description = "Manages preferences for the Spawner configuration.")
@XnatMixIn(ProxiedBeanMixIn.class)
@Slf4j
public class SpawnerPreferences extends EventTriggeringAbstractPreferenceBean {
    @Serial
    private static final long serialVersionUID = 1;
    public static final String SPAWNER_TOOL_ID = "spawner";

    @Autowired
    public SpawnerPreferences(final NrgPreferenceService preferenceService, final NrgEventServiceI eventService, final ConfigPaths configPaths, final OrderedProperties properties) {
        super(preferenceService, eventService, configPaths, properties);
    }

    @NrgPreference(defaultValue = "true")
    public boolean getPurgeAndRefreshOnStartup() {
        return getBooleanValue("purgeAndRefreshOnStartup");
    }

    public void setPurgeAndRefreshOnStartup(final boolean purgeAndRefreshOnStartup) {
        try {
            setBooleanValue(purgeAndRefreshOnStartup, "purgeAndRefreshOnStartup");
        } catch (InvalidPreferenceName invalidPreferenceName) {
            log.error("Invalid preference name 'purgeAndRefreshOnStartup': something is very wrong here.");
        }
    }
}
