//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Jul 25, 2005
 *
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
					context.put("search_element",data.getParameters().getString("search_element"));
					context.put("search_field",data.getParameters().getString("search_field"));
					context.put("search_value",data.getParameters().getString("search_value"));

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
