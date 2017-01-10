/*
 * web: org.nrg.xnat.turbine.modules.screens.SampleBuild
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XnatMrsessiondata;
import org.nrg.xdat.turbine.modules.screens.SecureReport;

public class SampleBuild extends SecureReport {

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void finalProcessing(RunData data, Context context) {
        XnatMrsessiondata mr = new XnatMrsessiondata(item);
        context.put("mr", mr);
    }

}
