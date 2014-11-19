/*
 * org.nrg.xdat.turbine.modules.actions.ModifyUser
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/3/14 9:54 AM
 */


package org.nrg.xdat.turbine.modules.actions;
import java.util.Hashtable;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.PasswordComplexityException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
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

		UserI authenticatedUser=TurbineUtils.getUser(data);
		
		UserI submitted=Users.createUser(TurbineUtils.GetDataParameterHash(data));
				
		String emailWithWhite = submitted.getEmail();
		if(emailWithWhite != null) {
			String noWhiteEmail = emailWithWhite.trim();
			submitted.setEmail(noWhiteEmail);
		}
		
		String login=submitted.getLogin();
		if(login==null){
			notifyAdmin(authenticatedUser, data,403,"Possible Authorization Bypass event", "User attempted to modify a user account other then his/her own.  This typically requires tampering with the HTTP form submission process.");
			return;
		}
		
		UserI oldUser=null;
		try {
			oldUser = Users.getUser(login);
		} catch (Exception e1) {
		}
		
		if(oldUser!=null && submitted.getID()==null){
			data.setMessage("User " + login + " already exists");
			data.setScreenTemplate("XDATScreen_edit_xdat_user.vm");
			return;
		}
		
        final Integer xdatUserId = submitted.getID();
		if (xdatUserId != null) {
            UserI byId=Users.getUser(xdatUserId);
			if(!byId.getLogin().equals(login)){
				data.setMessage("Unable to rename user accounts");
				data.setScreenTemplate("XDATScreen_edit_xdat_user.vm");
				return;
			}
		}

		String newPassword=data.getParameters().getString("xdat:user.primary_password"); // the object in submitted will have run the password through escape character encoding, potentially altering it
		
        if(StringUtils.isNotEmpty(newPassword)){
        	submitted.setVerified(Boolean.TRUE);
        	submitted.setPassword(newPassword);
		}else{
			data.setMessage("Password cannot be empty.");
			data.setScreenTemplate("XDATScreen_edit_xdat_user.vm");
			return;
		}
		
		try {
			Users.save(submitted, authenticatedUser,false,EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM,((oldUser==null))?"Added User "+login:"Modified User "+login));
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
				"search_value",submitted.getLogin());
		data.setAction("DisplayItemAction");
		VelocityAction action = (VelocityAction) ActionLoader.getInstance()
				.getInstance("DisplayItemAction");
		action.doPerform(data, context);
	}
}
