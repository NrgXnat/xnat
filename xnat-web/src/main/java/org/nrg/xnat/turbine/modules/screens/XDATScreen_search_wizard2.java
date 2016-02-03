/*
 * org.nrg.xnat.turbine.modules.screens.XDATScreen_search_wizard2
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.screens;

import java.util.Hashtable;
import java.util.Map;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;

public class XDATScreen_search_wizard2 extends SecureScreen {

	@Override
	protected void doBuildTemplate(RunData data, Context context) throws Exception {
		Map<String,Object> additional=new Hashtable<String,Object>();
		context.put("ELEMENT_0", TurbineUtils.GetPassedParameter("ELEMENT_0", data));
		for(ElementDisplay ed : UserHelper.getUserHelperService(TurbineUtils.getUser(data)).getSearchableElementDisplaysByPluralDesc()){
			if(TurbineUtils.HasPassedParameter("super_"+ed.getElementName(), data))
				additional.put(ed.getElementName(), TurbineUtils.GetPassedParameter("super_"+ed.getElementName(), data));
		}
		
		context.put("additional_types", additional);
	}
}
