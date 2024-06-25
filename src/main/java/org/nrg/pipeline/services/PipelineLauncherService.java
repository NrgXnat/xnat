package org.nrg.pipeline.services;


import org.nrg.pipeline.PipelineLaunchParameters;

public interface PipelineLauncherService {

    void setPipelineLaunchParameters(final PipelineLaunchParameters pipelineLaunchParameters);
    boolean launch();
    boolean launch(final String cmdPrefix);
}
