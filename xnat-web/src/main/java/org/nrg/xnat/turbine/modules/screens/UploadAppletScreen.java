package org.nrg.xnat.turbine.modules.screens;

import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.utils.AppletConfig;
import org.nrg.framework.services.SerializerService;

/**
 * Contains basic methods used by upload applet screen classes.
 */
public abstract class UploadAppletScreen extends SecureScreen {

    protected UploadAppletScreen() {
        _serializer = XDAT.getContextService().getBean(SerializerService.class);
    }

    protected org.nrg.config.entities.Configuration getAppletConfiguration(final UserI user, final String projectName) {
        //grab the applet config. Project level if it exists, otherwise, do the site-wide
        ConfigService configService = XDAT.getConfigService();

        // TODO: Are projectName and projectId really different here? If they're the same thing, some of this juggling can just go away.
        Scope scope;
        String projectId;
        try {
            if (projectName != null) {
                final XnatProjectdata p = XnatProjectdata.getXnatProjectdatasById(projectName, user, false);
                if (p != null) {
                    if (Permissions.canRead(user, "xnat:subjectData/project", p.getId())) {
                        projectId = (String) p.getItem().getProps().get("id");
                        scope = Scope.Project;
                    } else {
                        projectId = null;
                        scope = Scope.Site;
                    }
                } else {
                    projectId = null;
                    scope = Scope.Site;
                }
            } else {
                projectId = null;
                scope = Scope.Site;
            }
        } catch (Exception ignored) {
            projectId = null;
            scope = Scope.Site;
        }

        boolean enableProjectAppletScript = XDAT.getBoolSiteConfigurationProperty("enableProjectAppletScript", false);
        org.nrg.config.entities.Configuration config = enableProjectAppletScript ? configService.getConfig(AppletConfig.toolName, AppletConfig.path, scope, projectId) : null;

        if (config == null || org.nrg.config.entities.Configuration.DISABLED_STRING.equalsIgnoreCase(config.getStatus())) {
            //try to pull a site-wide config
            config = configService.getConfig(AppletConfig.toolName, AppletConfig.path);
        }
        return config;
    }

    protected SerializerService getSerializer() {
        return _serializer;
    }

    private final SerializerService _serializer;
}
