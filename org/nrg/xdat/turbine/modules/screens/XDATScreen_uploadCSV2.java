//Copyright 2006 Harvard University / Washington University School of Medicine All Rights Reserved
/*
 * Created on Oct 18, 2006
 *
 */
package org.nrg.xdat.turbine.modules.screens;

import java.io.File;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.ElementSecurity;
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
            File f = TurbineUtils.getUser(data).getCachedFile("csv/" + fm_id + ".xml");
            fm  = new FieldMapping(f);
        }
        
        context.put("fm_id", fm_id);
        context.put("fm", fm);
    }

}
