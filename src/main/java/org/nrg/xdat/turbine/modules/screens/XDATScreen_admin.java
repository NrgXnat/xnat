/*
 * org.nrg.xdat.turbine.modules.screens.XDATScreen_admin
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.screens;

import java.util.Hashtable;

import org.apache.turbine.services.pull.tools.TemplateLink;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTableI;
/**
 * @author Tim
 *
 */
public class XDATScreen_admin extends AdminScreen {
	public void doBuildTemplate(RunData data, Context context)
	{
		//TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
		XDATUser user = TurbineUtils.getUser(data);
		try {
			DisplaySearch search = user.getSearch(org.nrg.xft.XFT.PREFIX + ":user","listing");
			search.execute(new org.nrg.xdat.presentation.HTMLPresenter(TurbineUtils.GetContext(),false),TurbineUtils.getUser(data).getLogin());

			TurbineUtils.setSearch(data,search);

			Hashtable<String, String> tableProps = new Hashtable<String, String>();
			tableProps.put("bgColor","white"); 
			tableProps.put("border","0"); 
			tableProps.put("cellPadding","0"); 
			tableProps.put("cellSpacing","0"); 
			tableProps.put("width","95%");

            XFTTableI table = search.getPresentedTable();
            context.put("userTable", table.toHTML(false, "FFFFFF", "DEDEDE", tableProps, (search.getCurrentPageNum() * search.getRowsPerPage()) + 1));

            XdatUser boss = XdatUser.getXdatUsersByLogin("boss", user, false);
            if(boss != null && boss.getBooleanProperty("enabled")){
                TemplateLink link = (TemplateLink) context.get("link");
                String uri = link.setAction("DisplayItemAction").addPathInfo("search_value", "boss").addPathInfo("search_element", "xdat:user").addPathInfo("search_field", "xdat:user.login").getAbsoluteURI();
                data.setMessage(String.format("You currently have the user <b>boss</b> enabled. This user is deprecated and should be disabled to protect system security. <a href='%s'>Click here</a> to go to the boss user page.", uri));
            }

        } catch (Exception exception) {
			logger.warn("Error encountered retrieving user list", exception);
		}
	}
}

