/*
 * org.nrg.xdat.turbine.modules.actions.ModifyEmail
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/9/13 1:06 PM
 */


package org.nrg.xdat.turbine.modules.actions;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;

public class ModifyEmail extends SecureAction {

    static Logger logger = Logger.getLogger(ModifyEmail.class);
    public void doPerform(RunData data, Context context) throws Exception
    {
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
			data.addMessage("Unable to identify user for email modification.");
            if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null)
            {
                data.setScreenTemplate(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)));
            }
            return;
		}
		
		String oldEmail=existing.getEmail();
		if(found.getEmail()==null || StringUtils.equals(oldEmail, found.getEmail())){
			data.addMessage("Email address unchanged.");
            if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null)
            {
                data.setScreenTemplate(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)));
            }
            return;
		}
		
		existing.setEmail(found.getEmail());

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
       
                
        UserI authenticatedUser=(UserI)TurbineUtils.getUser(data);
        
        try {                	
        	if(existing.getEmail()!=null){
        		String newemail = existing.getEmail();
        		if(!newemail.contains("@")){
        			data.setMessage("Please enter a valid email address.");
        			data.setScreenTemplate("XDATScreen_UpdateUser.vm");
        			return;
        		}
        		Users.save(existing, authenticatedUser, false, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Modified User Email"));
            	
        		UserI item = TurbineUtils.getUser(data);
        		item.setEmail(existing.getEmail());
            	AdminUtils.sendUserHTMLEmail("Email address changed.", "Your email address was successfully changed to "+existing.getEmail() + ".", true, new String[]{authenticatedUser.getEmail(),existing.getEmail()});
            	data.setMessage("Email address changed.");
        	}else{
                data.setMessage("Email address unchanged.");
                data.setScreenTemplate("Index.vm");
        	}
        
        } catch (InvalidPermissionException e) {
			notifyAdmin(authenticatedUser, data,403,"Possible Authorization Bypass event", "User attempted to modify a user account other then his/her own.  This typically requires tampering with the HTTP form submission process.");
			return;
		} catch (Exception e) {
			logger.error("Error Storing User", e);
			return;
		}
                
         
        ElementSecurity.refresh();
            
        data.setScreenTemplate("Index.vm");

    }

}
