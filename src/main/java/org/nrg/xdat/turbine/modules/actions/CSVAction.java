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
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.turbine.utils.TurbineUtils;
/**
 * @author Tim
 *
 */
public class CSVAction extends SecureAction {
	public void doPerform(RunData data, Context context) throws Exception
	{
        preserveVariables(data,context);
	    if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("querytype",data)) !=null)
	    {
	        if(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("querytype",data)).equals("new"))
	        {
	            DisplaySearchAction dsa = new DisplaySearchAction();
	            DisplaySearch ds = dsa.setupSearch(data,context);
	            TurbineUtils.setSearch(data,ds);
                data.setScreenTemplate("CSVScreen.vm");
                return;
	        }
	    }
        
        if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("bundle",data)) !=null)
        {
            String bundle = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("bundle",data));
            BundleAction ba = new BundleAction();
            DisplaySearch ds = ba.setupSearch(data, context);
            TurbineUtils.setSearch(data,ds);
            data.setScreenTemplate("CSVScreen.vm");
            return;
        }

        
        if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_xml",data)) !=null)
        {
            data.setScreenTemplate("CSVScreen.vm");
            return;
        }
	    

        data.setScreenTemplate("CSVScreen.vm");
        return;
	}
}

