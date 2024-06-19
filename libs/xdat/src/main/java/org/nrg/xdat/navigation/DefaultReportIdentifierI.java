/*
 * core: org.nrg.xdat.navigation.DefaultReportIdentifierI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.navigation;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

public interface DefaultReportIdentifierI {
    String identifyReport(RunData data, Context context) throws Exception;
}