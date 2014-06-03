/*
 * org.nrg.xdat.turbine.modules.screens.XDATScreen_uploadCSV2
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.screens;

import java.io.File;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.utils.FieldMapping;

public class XDATScreen_uploadCSV2 extends SecureScreen {

    @Override
    protected void doBuildTemplate(RunData data, Context context)
            throws Exception {
        context.put("elements",ElementSecurity.GetNonXDATElementNames());

        FieldMapping fm = (FieldMapping)context.get("fm");
        String fm_id = (String)TurbineUtils.GetPassedParameter("fm_id", data);
        if (fm==null && fm_id!=null){
            File f = Users.getUserCacheFile(TurbineUtils.getUser(data),"csv/" + fm_id + ".xml");
            fm  = new FieldMapping(f);
        }
        
        context.put("fm_id", fm_id);
        context.put("fm", fm);
    }

}
