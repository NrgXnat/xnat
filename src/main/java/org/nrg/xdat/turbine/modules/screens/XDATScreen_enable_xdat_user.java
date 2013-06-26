//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 3, 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.services.UserRegistrationDataService;
import org.nrg.xdat.turbine.modules.actions.DisplayItemAction;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.schema.design.SchemaElementI;

/**
 * @author Tim
 *
 */
public class XDATScreen_enable_xdat_user extends AdminScreen {
	static Logger logger = Logger.getLogger(XDATScreen_enable_xdat_user.class);
	public void doBuildTemplate(RunData data, Context context)
	{
		try {
			ItemI o = TurbineUtils.GetItemBySearch(data);
			if (o != null)
			{		  
                boolean enabled = false;
				if(o.getBooleanProperty("enabled",true))
				{
					o.setProperty(XDATUser.USER_ELEMENT + ".enabled","0");
				}else{
                    enabled= true;
					o.setProperty(XDATUser.USER_ELEMENT + ".enabled","1");
				}

                XDATUser userI = new XDATUser(o);
				XDATUser.ModifyUser(TurbineUtils.getUser(data), userI,EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, (enabled)?"Enabled User":"Disabled User"));
                XDAT.getContextService().getBean(UserRegistrationDataService.class).clearUserRegistrationData(userI);

				SchemaElementI se = SchemaElement.GetElement(o.getXSIType());
				data = TurbineUtils.setDataItem(data,o);
                
                if (enabled)
                {
                    try {
                        AdminUtils.sendNewUserEmailMessage(userI.getUsername(), userI.getEmail(), context);
                    } catch (Exception e) {
                        logger.error("",e);
                    }
                }
                
				doRedirect(data,DisplayItemAction.GetReportScreen(se));
			}else{
			  	logger.error("No Item Found.");
			  	TurbineUtils.OutputDataParameters(data);
			  	doRedirect(data,"Index.vm");
			}
		} catch (Exception e) {
			logger.error("Enable",e);
			try {
				doRedirect(data,"Index.vm");
			} catch (Exception ignored) {}
		}
	}
}
