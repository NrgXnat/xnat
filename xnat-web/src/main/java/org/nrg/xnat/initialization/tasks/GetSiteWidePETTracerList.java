package org.nrg.xnat.initialization.tasks;

import org.apache.commons.io.FileUtils;
import org.nrg.config.entities.Configuration;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.PETTracerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GetSiteWidePETTracerList extends AbstractInitializingTask {
    @Override
    public String getTaskName() {
        return "Get site-wide PET tracer list";
    }

    @Override
    public void run() {
        try {
            final String path = PETTracerUtils.buildScriptPath(PETTracerUtils.ResourceScope.SITE_WIDE, "");
            final Configuration configuration = PETTracerUtils.getService().getTracerList(path, null);
            if (configuration == null) {
                _log.info("Creating PET Tracer List.");
                final String siteWide = FileUtils.readFileToString(PETTracerUtils.getDefaultTracerList());
                final UserI adminUser = getAdminUser();
                if (adminUser != null) {
                    PETTracerUtils.getService().setSiteWideTracerList(adminUser.getUsername(), path, siteWide);
                } else {
                    throw new Exception("Site administrator not found.");
                }
            }
            // there is a default site-wide tracer list, so nothing to do here for the else.
            complete();
        } catch (Throwable e){
            _log.error("Unable to either find or initialize the PET tracer list.", e);
        }
    }

    private UserI getAdminUser() throws Exception {
        for (String login : Users.getAllLogins()) {
            final UserI user = Users.getUser(login);
            if (Roles.isSiteAdmin(user)) {
                return user;
            }
        }
        return null;
    }

    private static final Logger _log = LoggerFactory.getLogger(GetSiteWidePETTracerList.class);
}
