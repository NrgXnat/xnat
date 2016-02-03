/*
 * org.nrg.xnat.turbine.modules.screens.DataTypeSearch
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;

public class DataTypeSearch extends SecureScreen {

	@Override
	protected void doBuildTemplate(RunData data, Context context)
			throws Exception {

		if(TurbineUtils.HasPassedParameter("dataType",data)){
			SchemaElement gwe = SchemaElement.GetElement((String)TurbineUtils.GetPassedParameter("dataType", data));
			
			context.put("dataType",TurbineUtils.GetPassedParameter("dataType", data));
			context.put("schemaElement",gwe);
		}
		
		data.getTemplateInfo().setLayoutTemplate("/ScreenOnly.vm");
	}

}
