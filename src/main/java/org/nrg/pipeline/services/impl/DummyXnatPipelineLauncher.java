package org.nrg.pipeline.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.pipeline.PipelineLaunchParameters;
import org.nrg.pipeline.services.PipelineLauncherService;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class DummyXnatPipelineLauncher implements PipelineLauncherService {

    @Override
    public void setPipelineLaunchParameters(final PipelineLaunchParameters pipelineLaunchParameters) {
        this.pipelineLaunchParameters = pipelineLaunchParameters;
    }

    @Override
    public  boolean launch() {
        log.error("XnatPipelineLauncher invoked without the Pipeline Engine Plugin");
        return true;
    }

    @Override
    public boolean launch(final String cmdPrefix) {
        log.error("XnatPipelineLauncher invoked without the Pipeline Engine Plugin");
        return true;
    }

    PipelineLaunchParameters pipelineLaunchParameters;
}
