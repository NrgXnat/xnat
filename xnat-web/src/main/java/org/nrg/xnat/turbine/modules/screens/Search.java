/*
 * web: org.nrg.xnat.turbine.modules.screens.Search
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;

public class Search extends SecureScreen {

	@Override
	protected void doBuildTemplate(RunData data, Context context)
			throws Exception {
		 if(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("node",data))!=null){
        	context.put("node", ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("node",data)));
         }
		 if(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("new_search",data))!=null){
	        	context.put("newSearch", "true");
	         }
	}

}
