package org.nrg.xnat.services.archive.impl.legacy;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.preferences.PipelinePreferences;
import org.nrg.xnat.services.archive.PipelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NoOpPipelineServiceImpl implements  PipelineService {

    @Autowired
    public NoOpPipelineServiceImpl(final PipelinePreferences preferences, final SiteConfigPreferences siteConfigPreferences) {
        this.preferences = preferences;
        this.siteConfigPreferences = siteConfigPreferences;
    }

    @Override
    public boolean launchAutoRun(final XnatExperimentdata experiment, final boolean suppressEmail, final UserI user) {
        log.error("AutoRun invoked without the Pipeline Engine Plugin");
        return true;
    }

    @Override
    public boolean launchAutoRun(final XnatExperimentdata experiment, final boolean suppressEmail, final UserI user, final boolean waitFor) {
        log.error("AutoRun invoked without the Pipeline Engine Plugin");
        return true;
    }

    private final PipelinePreferences      preferences;
    private final SiteConfigPreferences    siteConfigPreferences;
}