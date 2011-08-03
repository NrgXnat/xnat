//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 25, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions;
import java.util.Hashtable;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
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
		while (data.getParameters().get(header + counter) != null)
		{
			String elementToLoad = data.getParameters().getString(
					header + counter++);
			Integer numberOfInstances = data.getParameters().getIntObject(
					elementToLoad);
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
		try {
			ItemSearch search = new ItemSearch();
			search.setAllowMultiples(false);
			search.setElement("xdat:user");
			search.addCriteria("xdat:user.login", found.getProperty("login"));
			ItemI temp = search.exec().getFirst();
			if (temp == null)
			{
				// NEW USER
				String tempPass = found
						.getStringProperty("primary_password");
				if (!StringUtils.isEmpty(tempPass))
					found.setProperty("primary_password", XDATUser
							.EncryptString(tempPass,"SHA-256"));

				found.setProperty(
						"xdat:user.assigned_roles.assigned_role[0].role_name",
						"SiteUser");
				XDATUser newUser = new XDATUser(found);
				// newUser.initializePermissions();
				newUser.save(TurbineUtils.getUser(data), true, false, true,
						false);
			} else {
				// OLD USER
				String tempPass = found.getStringProperty("primary_password");
				String savedPass = temp.getStringProperty("primary_password");
				if (StringUtils.isEmpty(tempPass)
						&& StringUtils.isEmpty(savedPass)) {
					
				} else if (StringUtils.isEmpty(tempPass)) {
					found.setProperty("primary_password", "NULL");
				} else {
					if (!tempPass.equals(savedPass))
							found.setProperty("primary_password", XDATUser
									.EncryptString(tempPass,"SHA-256"));
				}
				found.save(TurbineUtils.getUser(data), false, false);
			}
			// UserCache.Clear();
			// if (temp == null)
			// {
			// AdminUtils.sendNewUserEmailMessage(found.getStringProperty("login"),found.getStringProperty("email"));
			//			    
			//			    
			// }
		} catch (Exception e) {
			logger.error("Error Storing User", e);
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
	// String header = "ELEMENT_";
	// int counter = 0;
	// Hashtable hash = new Hashtable();
	// while (data.getParameters().get(header + counter) != null)
	// {
	// String elementToLoad = data.getParameters().getString(header +
	// counter++);
	// Integer numberOfInstances =
	// data.getParameters().getInteger(elementToLoad);
	// if (numberOfInstances != null && numberOfInstances.intValue()!=0)
	// {
	// int subCount = 0;
	// while (subCount != numberOfInstances.intValue())
	// {
	// hash.put(elementToLoad + (subCount++),elementToLoad);
	// }
	// }else{
	// hash.put(elementToLoad,elementToLoad);
	// }
	// }
	//		
	// Enumeration keys = hash.keys();
	// while (keys.hasMoreElements())
	// {
	// String key = (String)keys.nextElement();
	// System.out.println("looking for " + key);
	// String element = (String)hash.get(key);
	// ItemI found = PopulateItem.Populate(data,element,key);
	// found.save();
	// }
}
