/*
 * core: org.nrg.xdat.turbine.modules.actions.ModifyPassword
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.actions;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

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
		if(data.getSession().getAttribute("forgot")!=null &&((Boolean)data.getSession().getAttribute("forgot"))){
            context.put("forgot", true);
        }

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
            redirect(data,false);
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
            redirect(data,false);
            return;
		}
		
		String newPassword=data.getParameters().getString("xdat:user.primary_password"); // the object in found will have run the password through escape character encoding, potentially altering it
		String oldPassword=existing.getPassword();
        String currentPassword = data.getParameters().getString("current_password");

		if((oldPassword==null || currentPassword==null || !oldPassword.equals(  (new ShaPasswordEncoder(256)).encodePassword(currentPassword, existing.getSalt())  )) && data.getSession().getAttribute("forgot")==null){
            //User correctly entered their old password or they forgot their old password
            data.setMessage("Incorrect current password. Password unchanged.");
            redirect(data,false);
            return;
        }
		existing.setPassword(newPassword);
		
		ValidationResultsI vr =Users.validate(existing);
        
		if(!vr.isValid()){
            redirect(data,false);
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
				}else{
					data.setMessage(validator.getMessage());
                    redirect(data,false);
					return;
				}
				
			}else{
			    data.setMessage("Password unchanged.");
                redirect(data,false);
			    return;
			}
			
		} catch (InvalidPermissionException e) {
			notifyAdmin(authenticatedUser, data,403,"Possible Authorization Bypass event", "User attempted to modify a user account other then his/her own.  This typically requires tampering with the HTTP form submission process.");
			return;
		} catch (PasswordComplexityException e){

			data.setMessage( e.getMessage());
			redirect(data,false);
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
        redirect(data, true);

	}

    private void redirect(RunData data, boolean changed){
        if(changed) {
            Boolean expired = (Boolean) data.getSession().getAttribute("expired");
            Boolean forgot = (Boolean) data.getSession().getAttribute("forgot");
            String loginTemplate = org.apache.turbine.Turbine.getConfiguration().getString("template.login");
            String homepageTemplate = org.apache.turbine.Turbine.getConfiguration().getString("template.homepage");
            if ((forgot != null && forgot)) {
                //User forgot their password. They must log in again.
                if (StringUtils.isNotEmpty(loginTemplate)) {
                    // We're running in a templating solution
                    data.setScreenTemplate(loginTemplate);
                } else {
                    data.setScreen(org.apache.turbine.Turbine.getConfiguration().getString("screen.login"));
                }
            }else if ((expired != null && expired)) {
                //They just updated expired password.
                data.getSession().setAttribute("expired",new Boolean(false));//New password is not expired
                if (StringUtils.isNotEmpty(homepageTemplate)) {
                    // We're running in a templating solution
                    data.setScreenTemplate(homepageTemplate);
                } else {
                    data.setScreen(org.apache.turbine.Turbine.getConfiguration().getString("screen.homepage"));
                }
            } else {
                data.setScreenTemplate("XDATScreen_UpdateUser.vm");
            }
        }
        else{
            data.setScreenTemplate("XDATScreen_UpdateUser.vm");
        }
    }

}
