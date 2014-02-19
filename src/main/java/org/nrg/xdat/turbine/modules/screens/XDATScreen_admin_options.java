/*
 * org.nrg.xdat.turbine.modules.screens.XDATScreen_admin_options
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;

public class XDATScreen_admin_options extends AdminScreen {

    @Override
    protected void doBuildTemplate(RunData data, Context context)
            throws Exception {
        XFTTable t = XFTTable.Execute("SELECT login, firstname,lastname FROM xdat_user;", TurbineUtils.getUser(data).getDBName(), null);
        context.put("users", t.rowHashs());
    }

}
