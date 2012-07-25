//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT ï¿½ Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 25, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions;
import java.io.IOException;
import java.util.Hashtable;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.XDATUser.PasswordComplexityException;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.search.ItemSearch;
/**
 * 
 * @author Tim
 * 
 * 
 */
public class ModifyUser extends SecureAction {
	static Logger logger = Logger.getLogger(ModifyUser.class);
	public void doPerform(RunData data, Context context) throws Exception
	{
		// TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
		// parameter specifying elementAliass and elementNames
		String header = "ELEMENT_";
		int counter = 0;
		Hashtable hash = new Hashtable();
		while (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(header + counter,data)) != null)
		{
			String elementToLoad = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(
					header + counter++,data));
			Integer numberOfInstances = ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedInteger(
					elementToLoad,data,null));
			if (numberOfInstances != null && numberOfInstances.intValue() != 0)
			{
				int subCount = 0;
				while (subCount != numberOfInstances.intValue())
				{
					hash.put(elementToLoad + (subCount++), elementToLoad);
				}
			} else {
				hash.put(elementToLoad, elementToLoad);
			}
		}
		
		
		PopulateItem populater = PopulateItem.Populate(data,
				org.nrg.xft.XFT.PREFIX + ":user", true);
		ItemI found = populater.getItem();
		String emailWithWhite = found.getStringProperty("email");
		String noWhiteEmail = emailWithWhite.trim();
		found.setProperty("email", noWhiteEmail);
		XDATUser authenticatedUser=TurbineUtils.getUser(data);
		
		String login=found.getStringProperty("login");
		if(login==null){
			notifyAdmin(authenticatedUser, data,403,"Possible Authorization Bypass event", "User attempted to modify a user account other then his/her own.  This typically requires tampering with the HTTP form submission process.");
			return;
		}
		
		XdatUser oldUser=XdatUser.getXdatUsersByLogin(login, null, false);
				
		try {
			XDATUser.ModifyUser(authenticatedUser, found,EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM,((oldUser==null))?"Add User "+login:"Modify User "+login));
		} catch (InvalidPermissionException e) {
			notifyAdmin(authenticatedUser, data,403,"Possible Authorization Bypass event", "User attempted to modify a user account other then his/her own.  This typically requires tampering with the HTTP form submission process.");
			return;
		} catch (PasswordComplexityException e){
			data.setMessage( e.getMessage());
			data.setScreenTemplate("XDATScreen_edit_xdat_user.vm");
			return;

		} catch (Exception e) {
			logger.error("Error Storing User", e);
			return;
		}
		data.getParameters().setString("search_element",
				org.nrg.xft.XFT.PREFIX + ":user");
		data.getParameters().setString("search_field",
				org.nrg.xft.XFT.PREFIX + ":user.login");
		data.getParameters().setString(
				"search_value",
				found.getProperty(
						org.nrg.xft.XFT.PREFIX + ":user" + XFT.PATH_SEPERATOR
								+ "login").toString());
		data.setAction("DisplayItemAction");
		VelocityAction action = (VelocityAction) ActionLoader.getInstance()
				.getInstance("DisplayItemAction");
		action.doPerform(data, context);
	}
}
