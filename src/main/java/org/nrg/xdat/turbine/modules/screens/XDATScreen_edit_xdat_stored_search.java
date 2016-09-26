/*
 * core: org.nrg.xdat.turbine.modules.screens.XDATScreen_edit_xdat_stored_search
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;
import java.util.Hashtable;
import java.util.List;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;


/**
 * @author XDAT
 *
 */
public class XDATScreen_edit_xdat_stored_search extends AdminEditScreenA {
	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XDATScreen_edit_xdat_stored_search.class);
	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#getElementName()
	 */
	public String getElementName() {
	    return "xdat:stored_search";
	}

	public ItemI getEmptyItem(RunData data) throws Exception
	{
	    String s = getElementName();
		ItemI temp =  XFTItem.NewItem(s,TurbineUtils.getUser(data));
		return temp;
	}
	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
	 */
	public void finalProcessing(RunData data, Context context) {
		List<? extends UserI> allUsers=Users.getUsers();
		Hashtable<String,String> users_h = new Hashtable<String,String>();
		
		for(UserI u: allUsers){
			String user = u.getLastname() + "," + u.getFirstname();
			users_h.put(u.getLogin(), user);
		}

		try {
			context.put("usernames", users_h);
			context.put("elements",ElementSecurity.GetNonXDATElementNames());
		} catch (Exception e) {
			logger.error("",e);
		}

		if (data.getParameters().containsKey("destination")){
			context.put("destination", TurbineUtils.escapeParam(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("destination",data))));
		}
	}
}
