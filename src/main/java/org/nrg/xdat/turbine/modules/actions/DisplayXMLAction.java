//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 19, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
/**
 * @author Tim
 *
 */
public class DisplayXMLAction extends SecureAction {
	static Logger logger = Logger.getLogger(DisplayXMLAction.class);
	public void doPerform(RunData data, Context context) throws Exception
	{
        preserveVariables(data,context);
		try {
			ItemI item = TurbineUtils.GetItemBySearch(data);
			if (item != null)
			{
				TurbineUtils.setDataItem(data,item);
				data.setScreenTemplate("XMLScreen.vm");
			}else{
				logger.error("No Item Found.");
				TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
				data.setScreenTemplate("Index.vm");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("No Item Found.");
			TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
			data.setScreenTemplate("Index.vm");
		}
	}
}

