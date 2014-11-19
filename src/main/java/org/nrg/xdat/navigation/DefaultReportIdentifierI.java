/*
 * org.nrg.xdat.navigation.DefaultReportIdentifierI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/21/14 9:52 AM
 */
package org.nrg.xdat.navigation;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

public interface DefaultReportIdentifierI {
    public String identifyReport(RunData data, Context context) throws Exception;
}
