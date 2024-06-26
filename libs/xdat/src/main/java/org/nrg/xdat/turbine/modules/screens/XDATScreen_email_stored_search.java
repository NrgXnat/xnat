/*
 * core: org.nrg.xdat.turbine.modules.screens.XDATScreen_email_stored_search
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.turbine.utils.TurbineUtils;


/**
 * @author Tim
 *
 */
public class XDATScreen_email_stored_search extends SecureScreen {
	/* (non-Javadoc)
	 * @see org.apache.turbine.modules.screens.VelocityScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
	 */
	public void doBuildTemplate(RunData data, Context context)
	{
		DisplaySearch search = TurbineUtils.getSearch(data);
		
		context.put("search",search);
        this.setLayout(data, "NoMenu.vm");
	}
}
