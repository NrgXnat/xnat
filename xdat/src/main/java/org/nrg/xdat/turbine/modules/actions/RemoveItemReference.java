/*
 * core: org.nrg.xdat.turbine.modules.actions.RemoveItemReference
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.actions;

import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xft.utils.SaveItemHelper;

/**
 * @author Tim
 *
 */
public class RemoveItemReference extends SecureAction {

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.actions.VelocityAction#doPerform(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void doPerform(PipelineData pipelineData, Context context) throws Exception {
        final RunData data = pipelineData.getRunData();
        
    }

}

