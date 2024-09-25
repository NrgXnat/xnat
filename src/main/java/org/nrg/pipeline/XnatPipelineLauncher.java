/*
 * web: org.nrg.pipeline.XnatPipelineLauncher
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.pipeline;

import org.nrg.pipeline.services.PipelineLauncherService;
import org.nrg.xdat.XDAT;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class XnatPipelineLauncher {

    public XnatPipelineLauncher(UserI user) {
        this.pipelineLaunchParameters = PipelineLaunchParameters.builder()
                .user(user)
                .build();
        pipelineLaunchParameters.notificationEmailId(user.getEmail(), XDAT.getSiteConfigPreferences().getAdminEmail());
        initParameters();
    }

    public XnatPipelineLauncher(final PipelineLaunchParameters pipelineLaunchParameters) {
        this.pipelineLaunchParameters = pipelineLaunchParameters;
        initParameters();
    }

    private void initParameters() {
        launcher.setPipelineLaunchParameters(pipelineLaunchParameters);
    }

    /*
     * Use this method when you want the job to be executed after schedule
     * command gets hold of the command string. Schedule could log the string
     * into a file and/or submit to a GRID
     */

    public boolean launch() {
        return launcher.launch();
    }

    /*
     * Setting cmdPrefix to null will launch the job directly.
     */

    public boolean launch(String cmdPrefix) {
        return launcher.launch(cmdPrefix);
    }

    @Autowired
    private PipelineLauncherService launcher;
    private PipelineLaunchParameters pipelineLaunchParameters;

}