/*
 * core: org.nrg.xdat.turbine.modules.screens.XDATScreen_uploadCSV
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;

import java.io.File;
import java.util.ArrayList;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.utils.FieldMapping;

public class XDATScreen_uploadCSV extends SecureScreen {

    @Override
    protected void doBuildTemplate(RunData data, Context context)
            throws Exception {
        context.put("elements",ElementSecurity.GetNonXDATElementNames());
        
        context.put("all_elements", GenericWrapperElement.GetAllElements(false));
        
        ArrayList<FieldMapping> fms = new ArrayList<FieldMapping>();
        File dir = Users.getUserCacheFile(TurbineUtils.getUser(data),"csv");
        File[] files = dir.listFiles();
        if (files!=null){
            for(File f : dir.listFiles()){
                fms.add(new FieldMapping(f));
            }
            
            context.put("fms",fms);
        }
    }

}
