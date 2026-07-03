/*
 * core: org.nrg.xdat.turbine.modules.screens.XDATScreen_edit
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperUtils;
import org.nrg.xft.schema.design.SchemaElementI;

/**
 * @author Tim
 *
 */
public class XDATScreen_edit extends SecureScreen {
	public void doBuildTemplate(PipelineData pipelineData, Context context)
	{
        RunData data = pipelineData.getRunData();
		try {
			ItemI item = TurbineUtils.GetItemBySearch(data);
			if (item == null)
			{
				try {
					String s = TurbineUtils.GetSearchElement(data);
					item = XFTItem.NewItem(s,TurbineUtils.getUser(data));
					SchemaElementI se = SchemaElement.GetElement(item.getXSIType());
					ItemI om = BaseElement.GetGeneratedItem(item);

					context.put("om", om);
					context.put("item",item);
					context.put("element",se);
					context.put("search_element",s);
					
				} catch (Exception e) {
					e.printStackTrace();
					data.setMessage("Invalid Search Parameters: No Data Item Found.");
					data.setScreen("Index");
					TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
				}
			}else{
				try {
					SchemaElementI se = SchemaElement.GetElement(item.getXSIType());
					ItemI om = BaseElement.GetGeneratedItem(item);

					context.put("om", om);
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
			}
		} catch (Exception e) {
			e.printStackTrace();
			data.setMessage("Invalid Search Parameters: No Data Item Found.");
			data.setScreen("Index");
			TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
		}
	}
}

