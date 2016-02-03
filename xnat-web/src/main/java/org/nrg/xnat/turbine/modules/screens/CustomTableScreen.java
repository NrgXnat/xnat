/*
 * org.nrg.xnat.turbine.modules.screens.CustomTableScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/3/14 9:54 AM
 */
package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xnat.restlet.rundata.RestletRunData;

public abstract class CustomTableScreen extends SecureScreen {

	public CustomTableScreen() {
		super();
	}

	@Override
	protected void doBuildTemplate(RunData data, Context context) throws Exception {
		XFTTable t=null;
		
		//this used to pass objects via the HTTP session, because the standard RunData can't have complex objects in its parameters.
		//customized the REST based RunData builder to pass the object in a different way.
		//but kept it backwards compatible in case there are modules out there using this
		if(TurbineUtils.HasPassedParameter("table_tag", data)){
			String tag=(String)TurbineUtils.GetPassedParameter("table_tag", data);
			t=(XFTTable)data.getSession().getAttribute(tag);
			
			context.put("table", t);
			data.getSession().removeAttribute(tag);
		}else if(data instanceof RestletRunData){
			t=(XFTTable)((RestletRunData)data).retrieveObject("table");
			context.put("table", t);
		}
		
		finalProcessing(t,data,context);
	}
	
	public void finalProcessing(XFTTable t, RunData data, Context context){
		
	}

}