/*
 * core: org.nrg.xdat.turbine.modules.actions.ListingAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.actions;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.turbine.utils.TurbineUtils;

public abstract class ListingAction extends SecureAction {

    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        preserveVariables(data,context);
        String destination = getDestinationScreenName(data);
        if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("querytype",data)) !=null)
        {
            if(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("querytype",data)).equals("new"))
            {
                DisplaySearchAction dsa = new DisplaySearchAction();
                DisplaySearch ds = dsa.setupSearch(data,context);
                TurbineUtils.setSearch(data,ds);
                data.setScreenTemplate(destination);
                return;
            }
        }
        
        if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("bundle",data)) !=null)
        {
            String bundle = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("bundle",data));
            BundleAction ba = new BundleAction();
            DisplaySearch ds = ba.setupSearch(data, context);
            TurbineUtils.setSearch(data,ds);
            data.setScreenTemplate(destination);
            return;
        }
        
        finalProcessing(data,context);

        data.setScreenTemplate(destination);
        return;
    }
    
    public void finalProcessing(RunData data,Context context) throws Exception {
        
    }

    public abstract String getDestinationScreenName(RunData data);
}
