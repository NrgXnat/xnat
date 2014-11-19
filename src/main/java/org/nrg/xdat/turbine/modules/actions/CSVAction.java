/*
 * org.nrg.xdat.turbine.modules.actions.CSVAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/9/13 1:06 PM
 */


package org.nrg.xdat.turbine.modules.actions;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;

/**
 * @author Tim
 */
public class CSVAction extends SecureAction {
    public void doPerform(RunData data, Context context) throws Exception {
        preserveVariables(data, context);
        data.setScreenTemplate("CSVScreen.vm");
        if (TurbineUtils.GetPassedParameter("querytype", data) != null && TurbineUtils.GetPassedParameter("querytype", data).equals("new")) {
            TurbineUtils.setSearch(data, new DisplaySearchAction().setupSearch(data, context));
        } else if (TurbineUtils.GetPassedParameter("bundle", data) != null) {
            TurbineUtils.setSearch(data, new BundleAction().setupSearch(data, context));
        }
    }
}

