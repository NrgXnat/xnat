package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.nrg.xnat.features.CustomFormsFeatureFlags;

public class MigrateLegacyCustomVariable extends SecureScreen {

    @Override
    protected void doBuildTemplate(PipelineData pipelineData, Context context) throws Exception {
        RunData data = pipelineData.getRunData();

        if (!XDAT.getBooleanPreferenceValue(CustomFormsFeatureFlags.CUSTOM_FORMS_FEATURE_FLAGS_TOOL_ID, CustomFormsFeatureFlags.CUSTOM_VARIABLE_MIGRATION_PREFERENCE_NAME, false)) {
            data.setMessage("Unauthorized: You do not have sufficient permission to access this page");
            data.setScreenTemplate("Error.vm");
            return;
        }
        UserI user = XDAT.getUserDetails();
        if (!Roles.isSiteAdmin(user) && !Roles.checkRole(user, CustomFormsConstants.FORM_MANAGER_ROLE)) {
            data.setMessage("Unauthorized: You do not have sufficient permission to access this page");
            data.setScreenTemplate("Error.vm");
            return;
        }

    }
}
