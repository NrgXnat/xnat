/*
 * org.nrg.xdat.turbine.modules.actions.BundleAction
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
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.turbine.utils.TurbineUtils;

/**
 * @author Tim
 *
 */
public class BundleAction extends SearchA {

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.actions.SearchA#setupSearch(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public DisplaySearch setupSearch(RunData data, Context context)
            throws Exception {
        DisplaySearch ds = null;
        if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("bundle",data))!=null)
        {
            String bundle = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("bundle",data));
            ds = TurbineUtils.getUser(data).getStoredSearch(bundle);
            if (ds ==null)
            {
                ds = null;
            }else{
                ds.setPagingOn(true);
                ds = addSearchCriteria(ds,data);
                return ds;
            }
        }
        
        if (ds==null){
            DisplaySearchAction dsa = new DisplaySearchAction();
            ds =dsa.setupSearch(data, context);
        }
        return null;
    }

    private DisplaySearch addSearchCriteria(DisplaySearch ds,RunData data) throws Exception
    {
        ds = setSearchCriteria(data,ds);
        
        return ds;
    }
}
