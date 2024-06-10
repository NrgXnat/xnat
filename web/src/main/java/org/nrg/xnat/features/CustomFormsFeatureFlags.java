package org.nrg.xnat.features;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.services.NrgPreferenceService;

@Slf4j
@NrgPreferenceBean(toolId = CustomFormsFeatureFlags.CUSTOM_FORMS_FEATURE_FLAGS_TOOL_ID,
        toolName = "Custom Forms Feature Flags",
        description = "Non-user-facing flags to disable in-development features related to Custom Forms"
)
public class CustomFormsFeatureFlags extends AbstractPreferenceBean {
    public static final String CUSTOM_FORMS_FEATURE_FLAGS_TOOL_ID = "custom-forms-features";
    public static final String PROJECT_OWNER_FORM_CREATION_PREFERENCE_NAME = "project-owner-form-creation";
    public static final String CUSTOM_VARIABLE_MIGRATION_PREFERENCE_NAME = "custom-variable-migration";
    public static final boolean DEFAULT_VALUE = false;

    public CustomFormsFeatureFlags(final NrgPreferenceService preferenceService) {
        super(preferenceService);
    }

    private boolean getBooleanValue(final String preferenceName, final boolean defaultValue) {
        return BooleanUtils.toBooleanDefaultIfNull(getBooleanValue(preferenceName), defaultValue);
    }

    @NrgPreference(defaultValue = "false")
    public boolean isProjectOwnerFormCreationEnabled() {
        return this.getBooleanValue(PROJECT_OWNER_FORM_CREATION_PREFERENCE_NAME, DEFAULT_VALUE);
    }

    @NrgPreference(defaultValue = "false")
    public boolean isCustomVariableMigrationEnabled() {
        return getBooleanValue(CUSTOM_VARIABLE_MIGRATION_PREFERENCE_NAME, DEFAULT_VALUE);
    }
}
