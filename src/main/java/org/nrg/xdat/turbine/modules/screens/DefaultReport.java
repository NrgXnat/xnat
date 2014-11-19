/*
 * org.nrg.xdat.turbine.modules.screens.DefaultReport
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.screens;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
/**
 * @author Tim
 *
 */
public class DefaultReport extends SecureReport {
	static Logger logger = Logger.getLogger(DefaultReport.class);
    public void finalProcessing(RunData data,Context context)
    {
        try {
            context.put("data_item",item.toHTML());
        } catch (Exception e) {
            logger.error("",e);
        }
    }
}

