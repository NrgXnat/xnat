/*
 * org.nrg.xdat.turbine.modules.actions.ModifyUserGroups
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/9/13 1:06 PM
 */


package org.nrg.xdat.turbine.modules.actions;

import java.util.Map;

import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;

public class ModifyUserGroups extends SecureAction {

    @Override
    public void doPerform(RunData data, Context context) throws Exception {
    	UserI newUser=Users.createUser(TurbineUtils.GetDataParameterHash(data));
        UserI oldUser=Users.getUser(newUser.getLogin());
        UserI authenticatedUser=TurbineUtils.getUser(data);

        if(authenticatedUser.isSiteAdmin()){
	        PersistentWorkflowI wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, TurbineUtils.getUser(data), Users.getUserDataType(),oldUser.getID().toString(),PersistentWorkflowUtils.ADMIN_EXTERNAL_ID, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Modified user settings"));
	        EventMetaI ci=wrk.buildEvent();
	        
	        try {
	            Map<String, UserGroupI> oldGroups=Groups.getGroupsForUser(oldUser);
	            Map<String, UserGroupI> newGroups=Groups.getGroupsForUser(newUser);
	            
	            //remove old groups no longer needed
	            for(UserGroupI uGroup : oldGroups.values()){
	                boolean matched=false;
	                if(!Groups.isMember(newUser, uGroup.getId())){
	                	Groups.removeUserFromGroup(oldUser, uGroup.getId(), ci);
	                }
	            }
	            
	            try {
	    			Users.save(newUser, authenticatedUser,false,ci);
	                
	                PersistentWorkflowUtils.complete(wrk, ci);
	    		} catch (InvalidPermissionException e) {
	                PersistentWorkflowUtils.fail(wrk, ci);
	    			notifyAdmin(authenticatedUser, data,403,"Possible Authorization Bypass event", "User attempted to modify a user account other then his/her own.  This typically requires tampering with the HTTP form submission process.");
	    			return;
	    		} catch (Exception e) {
	                PersistentWorkflowUtils.fail(wrk, ci);
	    			logger.error("Error Storing User", e);
	    			return;
	    		}
	            
	            
	        } catch (Exception e) {
	            PersistentWorkflowUtils.fail(wrk, ci);
	            logger.error("Error Storing User",e);
	        }
	        
	        data.getParameters().setString("search_element",org.nrg.xft.XFT.PREFIX + ":user");
	        data.getParameters().setString("search_field",org.nrg.xft.XFT.PREFIX + ":user.login");
	        
	        data.getParameters().setString("search_value",oldUser.getLogin());
	        data.setAction("DisplayItemAction");
	        VelocityAction action = (VelocityAction) ActionLoader.getInstance().getInstance("DisplayItemAction");
	        
	        if(!newUser.isEnabled() && oldUser.isEnabled()){
	        	//When a user is disabled, deactivate all their AliasTokens
	        	XDAT.getContextService().getBean(AliasTokenService.class).deactivateAllTokensForUser(newUser.getLogin());
	        }
	        else if (newUser.isEnabled() && !oldUser.isEnabled()){
	        	//When a user is enabled, notify the administrator
	        	try {
	                AdminUtils.sendNewUserEmailMessage(oldUser.getUsername(), oldUser.getEmail(), context);
	            } catch (Exception e) {
	                logger.error("",e);
	            }
	        }
	        action.doPerform(data, context);
        }
    }

}
