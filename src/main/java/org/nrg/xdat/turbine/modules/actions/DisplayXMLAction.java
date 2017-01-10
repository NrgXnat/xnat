/*
 * core: org.nrg.xdat.turbine.modules.actions.DisplayXMLAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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

