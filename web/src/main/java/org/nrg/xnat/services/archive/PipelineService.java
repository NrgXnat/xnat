package org.nrg.xnat.services.archive;

import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xft.security.UserI;

public interface PipelineService {
    /**
     * Launches the AutoRun pipeline on the specified experiment. This calls {@link #launchAutoRun(XnatExperimentdata,
     * boolean, UserI, boolean)}, with the last <b>waitFor</b> parameter set to <b>false</b>.
     *
     * @param experiment    The experiment for the AutoRun pipeline
     * @param suppressEmail Whether emails should be sent or suppressed on pipeline completion
     * @param user          The user requesting the AutoRun launch
     *
     * @return Returns <b>true</b> if the AutoRun pipeline was successfully launched, <b>false</b> otherwise
     */
    boolean launchAutoRun(XnatExperimentdata experiment, boolean suppressEmail, UserI user);

    /**
     * Launches the AutoRun pipeline on the specified experiment.
     *
     * @param experiment    The experiment for the AutoRun pipeline
     * @param suppressEmail Whether emails should be sent or suppressed on pipeline completion
     * @param user          The user requesting the AutoRun launch
     * @param waitFor       Indicates whether this should return on successful launch or wait until execution is completed.
     *
     * @return Returns <b>true</b> if the AutoRun pipeline was successfully launched, <b>false</b> otherwise
     */
    boolean launchAutoRun(XnatExperimentdata experiment, boolean suppressEmail, UserI user, boolean waitFor);
}
