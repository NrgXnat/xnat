/*
 * org.nrg.xdat.turbine.modules.screens.XDATScreen_password
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.utils.StringUtils;

/**
 * @author Tim
 *
 */
public class XDATScreen_password extends SecureScreen {

    public void doBuildTemplate(RunData data, Context context)
	{
		try {
			ItemI item = null;
			
			item = TurbineUtils.getUser(data).getItem();
			
			context.put("edit_screen",StringUtils.getLocalClassName(this.getClass()) + ".vm");
			try {
					SchemaElementI se = SchemaElement.GetElement(item.getXSIType());
					context.put("item",item);
					context.put("element",org.nrg.xdat.schema.SchemaElement.GetElement(item.getXSIType()));
					context.put("search_element",((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_element",data)));
					context.put("search_field",((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_field",data)));
					context.put("search_value",((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_value",data)));

				} catch (Exception e) {
					e.printStackTrace();
					data.setMessage("Invalid Search Parameters: No Data Item Found.");
					data.setScreen("Index");
					TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
				}

		} catch (Exception e) {
			e.printStackTrace();
			data.setMessage("Invalid Search Parameters: No Data Item Found.");
			data.setScreen("Index");
			TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
		}
	}

}
