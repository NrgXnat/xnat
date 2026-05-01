/*
 * core: org.nrg.xdat.turbine.modules.screens.XDATScreen_enable_xdat_user
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
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.services.UserRegistrationDataService;
import org.nrg.xdat.turbine.modules.actions.DisplayItemAction;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;

/**
 * @author Tim
 *
 */
public class XDATScreen_enable_xdat_user extends AdminScreen {
	static Logger logger = Logger.getLogger(XDATScreen_enable_xdat_user.class);
	public void doBuildTemplate(RunData data, Context context)
	{
		try {
			String login=(String)TurbineUtils.GetPassedParameter("search_value", data);
			if (login != null)
			{	
				UserI u=Users.getUser(login);
                boolean enabled = false;
                
				if(u.isEnabled())
				{
					Users.disableUser(u, TurbineUtils.getUser(data), EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Disabled User"));
				}else{
                    enabled= true;
                	Users.enableUser(u, TurbineUtils.getUser(data), EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Enabled User"));
    			}

                XDAT.getContextService().getBean(UserRegistrationDataService.class).clearUserRegistrationData(u);

                
                if (enabled)
                {
                    try {
                        AdminUtils.sendNewUserEmailMessage(u.getUsername(), u.getEmail());
                    } catch (Exception e) {
                        logger.error("",e);
                    }
                }else{
		        	//When a user is disabled, deactivate all their AliasTokens
		        	XDAT.getContextService().getBean(AliasTokenService.class).deactivateAllTokensForUser(u.getLogin());
                }

				data = TurbineUtils.setDataItem(data,(ItemI)Users.getUser(login));
				doRedirect(data,DisplayItemAction.GetReportScreen(Users.getUserDataType()));
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
