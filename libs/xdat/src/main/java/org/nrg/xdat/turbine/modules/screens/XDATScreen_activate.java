/*
 * core: org.nrg.xdat.turbine.modules.screens.XDATScreen_activate
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.turbine.modules.actions.ActivateAction;
import org.nrg.xdat.turbine.modules.actions.DisplayItemAction;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;

/**
 * @author Tim
 *
 */
public class XDATScreen_activate extends SecureScreen {
	static Logger logger = Logger.getLogger(XDATScreen_activate.class);
	public void doBuildTemplate(RunData data, Context context)
	{
	    try {
            ItemI item = null;
            try {
                item = ActivateAction.activate(data,context);
            } catch (Exception e2) {
                logger.error("",e2);
            }
            if (item ==null)
            {
                doRedirect(data,"Error.vm");
            }else{
                try {
                    String s = DisplayItemAction.GetReportScreen(new SchemaElement(((XFTItem)item).getGenericSchemaElement()));
                    doRedirect(data,s);
                } catch (ElementNotFoundException e) {
                    logger.error("",e);
                    doRedirect(data,"Error.vm");
                }
            }
        } catch (Exception e) {
            logger.error("",e);
        }
	}
}
