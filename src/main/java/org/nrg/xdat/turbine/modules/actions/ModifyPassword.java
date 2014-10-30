/*
 * org.nrg.xdat.turbine.modules.actions.ModifyPassword
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/3/14 9:54 AM
 */


package org.nrg.xdat.turbine.modules.actions;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.PasswordValidatorChain;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.PasswordComplexityException;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;

/**
 * @author Tim
 *
 */
public class ModifyPassword extends SecureAction {

    static Logger logger = Logger.getLogger(ModifyPassword.class);
	public void doPerform(RunData data, Context context) throws Exception
	{
		//TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
		//parameter specifying elementAliass and elementNames
		
		UserI user=TurbineUtils.getUser(data);
		if(user==null){
			error(new Exception("User 'null' cannot change password."), data);
		}

		if(user.getUsername().equals("guest")){
			error(new Exception("Guest account password must be managed in the administration section."), data);
		}
		
		UserI found;
    	try {
			found= Users.createUser(TurbineUtils.GetDataParameterHash(data));
		} catch (UserFieldMappingException e1) {
            data.addMessage(e1.getMessage());
            if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null)
            {
                data.setScreenTemplate(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)));
            }
            return;
		}
		
		UserI existing=null;
		if(found.getID()!=null){
			existing=Users.getUser(found.getID());
		}
		
		if(existing==null && found.getLogin()!=null){
			existing=Users.getUser(found.getLogin());
		}
		
		if(existing==null){
			data.addMessage("Unable to identify user for password modification.");
            if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null)
            {
                data.setScreenTemplate(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)));
            }
            return;
		}
		
		String newPassword=data.getParameters().getString("xdat:user.primary_password"); // the object in found will have run the password through escape character encoding, potentially altering it
		String oldPassword=existing.getPassword();
		
		existing.setPassword(newPassword);
		
		ValidationResultsI vr =Users.validate(existing);
        
		if(!vr.isValid()){
			TurbineUtils.SetEditItem(found,data);
            context.put("vr",vr);
            if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null)
            {
                data.setScreenTemplate(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)));
            }
            return;
		}
		
		UserI authenticatedUser=TurbineUtils.getUser(data);
		try {
            if(StringUtils.isNotEmpty(newPassword) && existing!=null){
				PasswordValidatorChain validator = XDAT.getContextService().getBean(PasswordValidatorChain.class);
				if(validator.isValid(newPassword, existing)){
					Users.save(existing, authenticatedUser, false,EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Modified User Password"));
					
					//need to update password expiration
					XdatUserAuth auth = XDAT.getXdatUserAuthService().getUserByNameAndAuth(existing.getUsername(), XdatUserAuthService.LOCALDB, "");
					auth.setPasswordUpdated(new java.util.Date());
					XDAT.getXdatUserAuthService().update(auth);
					
					data.getSession().setAttribute("expired",new Boolean(false));
				}else{
					data.setMessage(validator.getMessage());
					data.setScreenTemplate("XDATScreen_MyXNAT.vm");
					return;
				}
				
			}else{
			    data.setMessage("Password unchanged.");
			    data.setScreenTemplate("Index.vm");
			    return;
			}
			
		} catch (InvalidPermissionException e) {
			notifyAdmin(authenticatedUser, data,403,"Possible Authorization Bypass event", "User attempted to modify a user account other then his/her own.  This typically requires tampering with the HTTP form submission process.");
			return;
		} catch (PasswordComplexityException e){

			data.setMessage( e.getMessage());
			data.setScreenTemplate("XDATScreen_MyXNAT.vm");
			return;

		} catch (Exception e) {
			logger.error("Error Storing User", e);
			return;
		}
		
		SchemaElementI se = SchemaElement.GetElement(Users.getUserDataType());
		
		if (se.getGenericXFTElement().getType().getLocalPrefix().equalsIgnoreCase("xdat"))
		{
		    ElementSecurity.refresh();
		}
		data.setMessage("Password changed.");
		data.setScreenTemplate("Index.vm");

	}

}
