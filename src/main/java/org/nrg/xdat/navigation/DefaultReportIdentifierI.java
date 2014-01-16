package org.nrg.xdat.navigation;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

/**
 * Created by mmckay01 on 1/16/14.
 */
public interface DefaultReportIdentifierI {
    public String identifyReport(RunData data, Context context) throws Exception;
}
