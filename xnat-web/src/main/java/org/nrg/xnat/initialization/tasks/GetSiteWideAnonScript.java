package org.nrg.xnat.initialization.tasks;

import org.apache.commons.io.FileUtils;
import org.nrg.config.entities.Configuration;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.helpers.editscript.DicomEdit;
import org.nrg.xnat.helpers.merge.AnonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;

@Component
public class GetSiteWideAnonScript extends AbstractInitializingTask {
    @Autowired
    public GetSiteWideAnonScript(final SiteConfigPreferences preferences) {
        super();
        _preferences = preferences;
    }

    @Override
    public String getTaskName() {
        return "Get site-wide anon script";
    }

    @Override
    public void run() {
        try {
            final String path = DicomEdit.buildScriptPath(DicomEdit.ResourceScope.SITE_WIDE, "");
            final Configuration initConfig = AnonUtils.getService().getScript(path, null);
            if (initConfig == null) {
                _log.info("Creating Script Table.");
                final String siteWideScript = AnonUtils.getDefaultScript();
                final String adminUser = _preferences.getReceivedFileUser();
                if (adminUser != null) {
                    AnonUtils.getService().setSiteWideScript(adminUser, path, siteWideScript);
                } else {
                    throw new Exception("Site administrator not found.");
                }
            }
            // there is a default site-wide script, so nothing to do here for the else.
            complete();
        } catch (FileNotFoundException e) {
            _log.info("Couldn't find default anonymization script, waiting", e);
        } catch (Throwable e) {
            _log.error("Unable to either find or initialize script database", e);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(GetSiteWideAnonScript.class);

    private final SiteConfigPreferences _preferences;
}
