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
import org.nrg.framework.constants.Scope;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.preferences.EventTriggeringAbstractPreferenceBean;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.services.DataTypeAwareEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@SuppressWarnings("WeakerAccess")
@NrgPreferenceBean(toolId = PipelinePreferences.TOOL_ID,
                   toolName = "XNAT Traditional Pipeline Preferences",
                   description = "Manages preferences and settings for the XNAT traditional pipeline engine integration.")
@Slf4j
public class PipelinePreferences extends EventTriggeringAbstractPreferenceBean {
    public static final String TOOL_ID                         = "pipelines";
    public static final String AUTO_RUN_ENABLED                = "autoRunEnabled";
    public static final String ALLOW_AUTO_RUN_PROJECT_OVERRIDE = "allowAutoRunProjectOverride";

    @Autowired
    public PipelinePreferences(final NamedParameterJdbcTemplate template, final NrgPreferenceService preferenceService, final DataTypeAwareEventService eventService, final ConfigPaths configPaths, final OrderedProperties initPrefs) {
        super(preferenceService, eventService, configPaths, initPrefs);
        _template = template;
    }

    /**
     * Checks whether the AutoRun pipeline is enabled for the site. This setting can be overridden at the project level.
     * Check whether AutoRun is enabled for a particular project by calling {@link #isAutoRunEnabled(String)}.
     *
     * @return Returns <b>true</b> if the AutoRun pipeline is enabled by default site-wide, <b>false</b> otherwise.
     *
     * @see #setAutoRunEnabled(boolean)
     */
    @NrgPreference(defaultValue = "false")
    public boolean isAutoRunEnabled() {
        return getBooleanValue(AUTO_RUN_ENABLED);
    }

    /**
     * Enables or disables the AutoRun pipeline at the site level.
     *
     * @param autoRunEnabled Whether AutoRun should be enabled or disabled
     *
     * @see #isAutoRunEnabled()
     * @see #setAutoRunEnabled(String, boolean)
     */
    public void setAutoRunEnabled(final boolean autoRunEnabled) {
        try {
            setBooleanValue(autoRunEnabled, AUTO_RUN_ENABLED);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name autoRunEnabled: something is very wrong here.", e);
        }
    }

    /**
     * Checks whether the AutoRun pipeline is enabled for the specified project.
     *
     * @param projectId The ID of the project to check
     *
     * @return Returns <b>true</b> if the AutoRun pipeline is enabled for the specified project, <b>false</b> otherwise.
     *
     * @see #setAutoRunEnabled(String, boolean)
     */
    public boolean isAutoRunEnabled(final String projectId) throws NotFoundException {
        if (!Permissions.verifyProjectExists(_template, projectId)) {
            throw new NotFoundException(XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId);
        }
        return isAllowAutoRunProjectOverride() ? getBooleanValue(Scope.Project, projectId, AUTO_RUN_ENABLED) : isAutoRunEnabled();
    }

    /**
     * Enables or disables the AutoRun pipeline for the specified project.
     *
     * @param projectId      The ID of the project to set
     * @param autoRunEnabled Whether AutoRun should be enabled or disabled
     *
     * @see #isAutoRunEnabled(String)
     * @see #setAutoRunEnabled(boolean)
     */
    public void setAutoRunEnabled(final String projectId, final boolean autoRunEnabled) throws NotFoundException {
        if (!Permissions.verifyProjectExists(_template, projectId)) {
            throw new NotFoundException(XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId);
        }
        try {
            setBooleanValue(Scope.Project, projectId, autoRunEnabled, AUTO_RUN_ENABLED);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name autoRunEnabled: something is very wrong here.", e);
        }
    }

    /**
     * Checks whether AutoRun can be enabled or disabled for specific projects.
     *
     * @return Returns <b>true</b> if autorun can be enabled or disabled for specific projects, <b>false</b> otherwise.
     */
    @NrgPreference(defaultValue = "false")
    public boolean isAllowAutoRunProjectOverride() {
        return getBooleanValue(ALLOW_AUTO_RUN_PROJECT_OVERRIDE);
    }

    /**
     * Specifies whether AutoRun can be enabled or disabled for specific projects.
     *
     * @param allowAutoRunProjectOverride Whether autorun can be enabled or disabled on a project-by-project basis
     **/
    public void setAllowAutoRunProjectOverride(final boolean allowAutoRunProjectOverride) {
        try {
            setBooleanValue(allowAutoRunProjectOverride, ALLOW_AUTO_RUN_PROJECT_OVERRIDE);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name allowAutoRunProjectOverride: something is very wrong here.", e);
        }
    }

    private final NamedParameterJdbcTemplate _template;
}
