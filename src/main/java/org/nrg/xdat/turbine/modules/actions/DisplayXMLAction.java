/*
 * org.nrg.xdat.turbine.modules.actions.DisplayXMLAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/9/13 1:06 PM
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

