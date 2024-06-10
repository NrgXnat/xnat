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

@NrgPreferenceBean(toolId = FileStorePreferences.TOOL_ID,
                   toolName = "XNAT File Store Service Preferences",
                   description = "Manages preferences and settings for the XNAT file-store service.")
@Slf4j
public class FileStorePreferences extends EventTriggeringAbstractPreferenceBean {
    public static final String TOOL_ID = "fileStore";

    @Autowired
    public FileStorePreferences(final NrgPreferenceService preferenceService, final DataTypeAwareEventService eventService, final ConfigPaths configPaths, final OrderedProperties initPrefs) {
        super(preferenceService, eventService, configPaths, initPrefs);
    }

    @NrgPreference(defaultValue = "/data/xnat/fileStore")
    public String getFileStorePath() {
        return getValue("fileStorePath");
    }

    @SuppressWarnings("unused")
    public void setFileStorePath(final String fileStorePath) {
        try {
            set(fileStorePath, "fileStorePath");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name fileStorePath: something is very wrong here.", e);
        }
    }
}
