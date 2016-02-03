/*
 * org.nrg.xnat.turbine.modules.actions.DisplayMRSuperSearchAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.actions;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.turbine.modules.actions.DisplaySearchAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;

import java.util.ArrayList;
import java.util.Enumeration;

/**
 * @author Tim
 *
 */
public class DisplayMRSuperSearchAction extends DisplaySearchAction {
    /* (non-Javadoc)
     * @see org.cnl.xdat.turbine.modules.actions.SearchA#setupSearch(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public DisplaySearch setupSearch(RunData data, Context context) throws Exception {
        String elementName= ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("ELEMENT_0",data));
        SchemaElement se = SchemaElement.GetElement(elementName);
        
        DisplaySearch ds = getSearchCriteria(se, elementName, data);
    	//logger.error(ds.getCriteriaCollection().toString());
        
        return addAdditionalViews(ds,data);
    }
    
    private DisplaySearch addAdditionalViews(DisplaySearch ds, RunData data)
    {
        ArrayList found = new ArrayList();
   //TurbineUtils.OutputDataParameters(data);
		Enumeration enumer = DisplayManager.GetInstance().getElements().keys();
		while (enumer.hasMoreElements())
		{
			String key = (String)enumer.nextElement();
			if (TurbineUtils.HasPassedParameter("super_" + key.toLowerCase() + "_detailed",data))
			{
			    String s = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("super_" + key.toLowerCase() + "_detailed",data));
			    if (! s.equalsIgnoreCase(""))
			        ds.addAdditionalView(key,s);
			}else if(TurbineUtils.HasPassedParameter("super_" + key.toLowerCase() + "_brief",data)){
			    String s = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("super_" + key.toLowerCase() + "_brief",data));
			    if (! s.equalsIgnoreCase(""))
			        ds.addAdditionalView(key,s);
			}
		}
		
		
		if (ds.getAdditionalViews().size() > 0)
		{
			if (ds.getRootElement().getDisplay().getVersion("root")!=null)
			{
			    ds.setDisplay("root");
			}
		}

        return ds;
    }
}
