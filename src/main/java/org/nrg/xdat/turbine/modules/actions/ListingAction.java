//Copyright Washington University School of Medicine All Rights Reserved
/*
 * Created on Mar 5, 2007
 *
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
        if (data.getParameters().get("querytype") !=null)
        {
            if(data.getParameters().getString("querytype").equals("new"))
            {
                DisplaySearchAction dsa = new DisplaySearchAction();
                DisplaySearch ds = dsa.setupSearch(data,context);
                TurbineUtils.setSearch(data,ds);
                data.setScreenTemplate(destination);
                return;
            }
        }
        
        if (data.getParameters().get("bundle") !=null)
        {
            String bundle = data.getParameters().get("bundle");
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
