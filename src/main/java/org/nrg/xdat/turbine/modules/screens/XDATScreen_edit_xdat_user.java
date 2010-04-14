//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 21, 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
/**
 * @author Tim
 *
 */
public class XDATScreen_edit_xdat_user extends AdminScreen {
	public void doBuildTemplate(RunData data, Context context)
	{
		try {
			ItemI item = TurbineUtils.GetItemBySearch(data);
			if (item == null)
			{
				try {
					item = XFTItem.NewItem(org.nrg.xft.XFT.PREFIX + ":user",TurbineUtils.getUser(data));
					item.setProperty("enabled",Boolean.FALSE);
					context.put("item",item);
					context.put("element",org.nrg.xdat.schema.SchemaElement.GetElement(item.getXSIType()));
					context.put("search_element",org.nrg.xft.XFT.PREFIX + ":user");
				} catch (Exception e) {
					e.printStackTrace();
					data.setMessage("Invalid Search Parameters: No Data Item Found.");
					data.setScreen("Index");
					TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
				}
			}else{
				try {
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
			}
		} catch (Exception e) {
			e.printStackTrace();
			data.setMessage("Invalid Search Parameters: No Data Item Found.");
			data.setScreen("Index");
			TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
		}
	}
}

