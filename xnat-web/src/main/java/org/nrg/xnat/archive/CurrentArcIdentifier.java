package org.nrg.xnat.archive;

import org.nrg.xdat.om.base.BaseXnatExperimentdata;

/**
 * Created by jordan on 7/11/17.
 */
public class CurrentArcIdentifier implements CurrentArcIdentifierI {
    @Override
    public String identify(BaseXnatExperimentdata expt){
        return "default";
    }
}
