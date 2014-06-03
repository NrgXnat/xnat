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

import java.util.Hashtable;

import org.apache.turbine.services.pull.tools.TemplateLink;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.security.UserI;
/**
 * @author Tim
 *
 */
public class XDATScreen_admin extends AdminScreen {
	public void doBuildTemplate(RunData data, Context context)
	{
		//TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
		UserI user = TurbineUtils.getUser(data);
		try {
			DisplaySearch search = UserHelper.getSearchHelperService().getSearchForUser(TurbineUtils.getUser(data),org.nrg.xft.XFT.PREFIX + ":user","listing");
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

            UserI boss = Users.getUser("boss");
            if(boss != null && boss.isEnabled()){
                TemplateLink link = (TemplateLink) context.get("link");
                String uri = link.setAction("DisplayItemAction").addPathInfo("search_value", "boss").addPathInfo("search_element", "xdat:user").addPathInfo("search_field", "xdat:user.login").getAbsoluteURI();
                data.setMessage(String.format("You currently have the user <b>boss</b> enabled. This user is deprecated and should be disabled to protect system security. <a href='%s'>Click here</a> to go to the boss user page.", uri));
            }

        } catch (Exception exception) {
			logger.warn("Error encountered retrieving user list", exception);
		}
	}
}

