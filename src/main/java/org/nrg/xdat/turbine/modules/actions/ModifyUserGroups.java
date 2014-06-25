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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XdatRoleType;
import org.nrg.xdat.om.XdatUserGroupid;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.turbine.utils.AdminUtils;

public class ModifyUserGroups extends SecureAction {

    @Override
    public void doPerform(RunData data, Context context) throws Exception {
//      TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
        //parameter specifying elementAliass and elementNames
        String header = "ELEMENT_";
        int counter = 0;
        Hashtable hash = new Hashtable();
        while (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(header + counter,data)) != null)
        {
            String elementToLoad = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(header + counter++,data));
            Integer numberOfInstances = ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedInteger(elementToLoad,data));
            if (numberOfInstances != null && numberOfInstances.intValue()!=0)
            {
                int subCount = 0;
                while (subCount != numberOfInstances.intValue())
                {
                    hash.put(elementToLoad + (subCount++),elementToLoad);
                }
            }else{
                hash.put(elementToLoad,elementToLoad);
            }
        }
        
        
        PopulateItem populater = PopulateItem.Populate(data,org.nrg.xft.XFT.PREFIX + ":user",true);
        ItemI found = populater.getItem();
        
        ItemSearch search = new ItemSearch();
        search.setAllowMultiples(false);
        search.setElement("xdat:user");
        search.addCriteria("xdat:user.login",found.getProperty("login"));
        
        ItemI temp = search.exec().getFirst();

        XDATUser oldUser = new XDATUser(temp);
        XDATUser newUser = new XDATUser(found);

        PersistentWorkflowI wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, TurbineUtils.getUser(data), found.getXSIType(),oldUser.getStringProperty("xdat_user_id"),PersistentWorkflowUtils.ADMIN_EXTERNAL_ID, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Modified user settings"));
        EventMetaI ci=wrk.buildEvent();
        
        try {
            
            ArrayList<XdatUserGroupid> newGroups = newUser.getGroups_groupid();
            ArrayList<XdatUserGroupid> oldGroups = oldUser.getGroups_groupid();
            
            for(XdatUserGroupid uGroup : oldGroups){
                boolean matched=false;
                for (XdatUserGroupid newGroup:newGroups){
                    if (newGroup.getGroupid().equals(uGroup.getGroupid())){
                        matched=true;
                    }
                }
                
                if (!matched){
                	SaveItemHelper.unauthorizedDelete(uGroup.getItem(), TurbineUtils.getUser(data),ci);
                }
            }

            XDATUser authenticatedUser=TurbineUtils.getUser(data);
            try {
    			XDATUser.ModifyUser(authenticatedUser, found,ci);
                found.getItem().removeEmptyItems();
                
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
            
            
            
//            UserCache.Clear();
            
            
//          if (temp == null)
//          {
//              AdminUtils.sendNewUserEmailMessage(found.getStringProperty("login"),found.getStringProperty("email"));
//              
//              
//          }
        } catch (Exception e) {
            PersistentWorkflowUtils.fail(wrk, ci);
            logger.error("Error Storing User",e);
        }
        
        data.getParameters().setString("search_element",org.nrg.xft.XFT.PREFIX + ":user");
        data.getParameters().setString("search_field",org.nrg.xft.XFT.PREFIX + ":user.login");
        
        data.getParameters().setString("search_value",found.getProperty(org.nrg.xft.XFT.PREFIX + ":user" + XFT.PATH_SEPERATOR + "login").toString());
        data.setAction("DisplayItemAction");
        VelocityAction action = (VelocityAction) ActionLoader.getInstance().getInstance("DisplayItemAction");
        
        if(found.getStringProperty("enabled").equals("false")){
        	//When a user is disabled, deactivate all their AliasTokens
        	XDAT.getContextService().getBean(AliasTokenService.class).deactivateAllTokensForUser(found.getStringProperty("login"));
        }
        else if (found.getStringProperty("enabled").equals("true")){
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
