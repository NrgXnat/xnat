//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT  Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 25, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.PermissionCriteria;
import org.nrg.xdat.security.PermissionItem;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.search.TableSearch;
/**
 * @author Tim
 *
 */
public class ModifyUserPrivileges extends SecureAction {
	static Logger logger = Logger.getLogger(ModifyUserPrivileges.class);


	public void doEmail(RunData data, Context context) throws Exception
	{
		final XDATUser tempUser = storeChanges(data,context,(String)TurbineUtils.GetPassedParameter("message",data));
	
		if (tempUser.needsActivation())
		{
		    AdminUtils.sendAuthorizationEmailMessage(tempUser);
		}
		
		TurbineUtils.setDataItem(data,tempUser.getItem());
		data.getParameters().setString("search_element",org.nrg.xft.XFT.PREFIX + ":user");
		data.getParameters().setString("search_field",org.nrg.xft.XFT.PREFIX + ":user.login");
		data.getParameters().setString("search_value",tempUser.getUsername());
		data.setAction("DisplayItemAction");
		VelocityAction action = (VelocityAction) ActionLoader.getInstance().getInstance("DisplayItemAction");
		action.doPerform(data, context);
		ElementSecurity.refresh();
	}	
	
	public void doPerform(RunData data, Context context) throws Exception
	{
		final XDATUser tempUser = storeChanges(data,context,(String)TurbineUtils.GetPassedParameter("message",data));
		
		TurbineUtils.setDataItem(data,tempUser.getItem());
		data.getParameters().setString("search_element",org.nrg.xft.XFT.PREFIX + ":user");
		data.getParameters().setString("search_field",org.nrg.xft.XFT.PREFIX + ":user.login");
		data.getParameters().setString("search_value",tempUser.getUsername());
		data.setAction("DisplayItemAction");
		final VelocityAction action = (VelocityAction) ActionLoader.getInstance().getInstance("DisplayItemAction");
		action.doPerform(data, context);
		ElementSecurity.refresh();
	}	
	
	/**
	 * @param tempUser
	 * @param props
	 * @return
	 * @throws Exception
	 */
	public static XDATUser SetUserProperties(XDATUser tempUser,Map<String,? extends Object> props, String userName) throws Exception
	{
	    final ArrayList<ElementSecurity> elements = ElementSecurity.GetSecureElements();
		for (ElementSecurity es:elements)
		{
			final List<PermissionItem> permissionItems = es.getPermissionItems(userName);
			for (final PermissionItem pi:permissionItems)
			{
				final PermissionCriteria pc = new PermissionCriteria();

				pc.setField(pi.getFullFieldName());
				pc.setFieldValue(pi.getValue());
				final String s = es.getElementName()+ "_" + pi.getFullFieldName() + "_" + pi.getValue();
				if (props.get(s.toLowerCase() + "_r") != null)
				{
			    	pc.setRead(true);
				}else{
					pc.setRead(false);
				}
				if (props.get(s.toLowerCase() + "_c") != null)
				{
					pc.setCreate(true);
				}else{
					pc.setCreate(false);
				}
				if (props.get(s.toLowerCase() + "_e") != null)
				{
					pc.setEdit(true);
				}else{
					pc.setEdit(false);
				}
				if (props.get(s.toLowerCase() + "_d") != null)
				{
					pc.setDelete(true);
				}else{
					pc.setDelete(false);
				}
			    if (props.get(s.toLowerCase() + "_a") != null)
				{
				    pc.setActivate(true);
				}else{
				    pc.setActivate(false);
				}
				if (props.get(s.toLowerCase() + "_type") != null)
				{
				   pc.setComparisonType((String)props.get(s.toLowerCase() + "_type"));
				}
				
				final String wasSet=(String)props.get(s.toLowerCase() + "_wasSet");
				
				if(wasSet.equals("1") || pc.getCreate() || pc.getRead() || pc.getEdit() || pc.getDelete() || pc.getActivate()){
					tempUser.addRootPermission(es.getElementName(),pc);
				}
			}
		}
		
		
		return tempUser;
	}
	
	public XDATUser storeChanges(RunData data,Context context,String message) throws Exception
	{
//	  TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
		//parameter specifying elementAliass and elementNames
		XDATUser authenticatedUser=TurbineUtils.getUser(data);
		
		//populate item from data parameters
		final PopulateItem populater = PopulateItem.Populate(data,"xdat:user",true);
		final ItemI found = populater.getItem();
	    
	    XDATUser tempUser = new XDATUser(found);
	    
		final String login = tempUser.getUsername();
	    
		XdatUser oldUser=XdatUser.getXdatUsersByLogin(login, null, false);
		
		if(oldUser==null){
			throw new Exception("User must already exist to make this modification");
		}
		
		//create workflow entry to track action
		PersistentWorkflowI wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, TurbineUtils.getUser(data), found.getXSIType(),oldUser.getStringProperty("xdat_user_id"),PersistentWorkflowUtils.ADMIN_EXTERNAL_ID, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN,EventUtils.TYPE.WEB_FORM,"Modified User Permissions"));
        		
	    EventMetaI ci=wrk.buildEvent();
	    
	    //save passed modifications
	    try {
	    	XDATUser.ModifyUser(authenticatedUser, tempUser,ci);
		} catch (Exception e) {
			PersistentWorkflowUtils.fail(wrk,ci);
			logger.error("Error Storing User",e);
			return authenticatedUser;
		}
		
		//clear out any roles that were removed
		final ItemCollection items = ItemSearch.GetItems("xdat:user.login",login,TurbineUtils.getUser(data),false);
	    if (items.size()>0)
	    {
	    	final XFTItem item = (XFTItem)items.getFirst();
	    	final ArrayList<XFTItem> newItems = found.getChildItems("xdat:user.assigned_roles.assigned_role");
	    	final ArrayList<XFTItem> oldItems = item.getChildItems("xdat:user.assigned_roles.assigned_role");
	        for (XFTItem oldChild: oldItems)
            {
                boolean foundChild = false;
                for (XFTItem newChild:newItems)
                {
                    if (XFTItem.CompareItemsByPKs(newChild,oldChild))
                    {
                        foundChild = true;
                        break;
                    }
                }
                
                if (!foundChild)
                {
                    item.removeChildFromDB("xdat:user.assigned_roles.assigned_role",oldChild,TurbineUtils.getUser(data),ci);
                }
            }
	    }
		
		tempUser = new XDATUser(found.getCurrentDBVersion());

		//logger.error("3\n"+tempUser.getItem().toString());
		final Map<String,String> props = TurbineUtils.GetDataParameterHash(data);
		
		tempUser = SetUserProperties(tempUser,props,TurbineUtils.getUser(data).getLogin());

		//logger.error("4\n"+tempUser.getItem().toString());
		try {
			XDATUser.ModifyUser(authenticatedUser, tempUser.getItem(),ci);
		} catch (InvalidPermissionException e) {
			notifyAdmin(authenticatedUser, data,403,"Possible Authorization Bypass event", "User attempted to modify a user account other then his/her own.  This typically requires tampering with the HTTP form submission process.");
			PersistentWorkflowUtils.fail(wrk,ci);
			return authenticatedUser;
		} catch (Exception e) {
			logger.error("Error Storing User", e);
			PersistentWorkflowUtils.fail(wrk,ci);
			return authenticatedUser;
		}
		
		//UPDATE BUNDLES
		final XFTTable presetBundles = TableSearch.Execute("SELECT xdat_stored_search_id, login, xdat_stored_search_id,xdat_stored_search_allowed_user_id FROM xdat_stored_search_allowed_user WHERE login='"+ tempUser.getProperty("login") + "';",tempUser.getDBName(),TurbineUtils.getUser(data).getLogin());
		
		final XFTTable allbundles = TableSearch.Execute("SELECT id FROM xdat_stored_search WHERE tag IS NULL;",tempUser.getDBName(),TurbineUtils.getUser(data).getLogin());
				
		boolean bundleChange = false;
		allbundles.resetRowCursor();
		while (allbundles.hasMoreRows());
		{
			final Map<Object,Object> nextRow = allbundles.nextRowHash();
		    String bundleID=(String)nextRow.get("id");
		    if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("bundle_" + bundleID.toLowerCase(), data)) !=null)
		    {
		    	final Map<Object,Object> rowHash = presetBundles.getRowHash("xdat_stored_search_id",bundleID);
		        if (rowHash == null)
		        {
		            //REMOVE USER-BUNDLE LINK
		            PoolDBUtils.ExecuteNonSelectQuery("INSERT INTO xdat_stored_search_allowed_user (xdat_stored_search_id, login) VALUES ('" + bundleID + "','" + tempUser.getProperty("login") + "');",tempUser.getDBName(),TurbineUtils.getUser(data).getLogin());
		            bundleChange = true;
		        }
		    }else{
		    	final Map<Object,Object> rowHash = presetBundles.getRowHash("xdat_stored_search_id",bundleID);
		        if (rowHash != null)
		        {
		            //REMOVE USER-BUNDLE LINK
		            Object o = rowHash.get("xdat_stored_search_allowed_user_id");
		            PoolDBUtils.ExecuteNonSelectQuery("DELETE FROM xdat_stored_search_allowed_user WHERE xdat_stored_search_allowed_user_id=" + o + ";",tempUser.getDBName(),TurbineUtils.getUser(data).getLogin());
		            bundleChange = true;
		        }
		    }
		}
		
		PersistentWorkflowUtils.complete(wrk,ci);
		
        PoolDBUtils.PerformUpdateTrigger(tempUser.getItem(), TurbineUtils.getUser(data).getLogin());
		
		if (bundleChange)
		{
		    ElementSecurity.refresh();
		}

//	    UserCache.Clear();
		return tempUser;
	}
	
	public void doPrint(RunData data, Context context)
	   throws Exception
	{
		if(XFT.VERBOSE)System.out.println("ModifyUserPriviledges doPrint()"); 
		final XDATUser tempUser = storeChanges(data,context,(String)TurbineUtils.GetPassedParameter("message",data));
		ElementSecurity.refresh();
		
		TurbineUtils.setDataItem(data,tempUser.getItem());
		data.getParameters().setString("search_element",org.nrg.xft.XFT.PREFIX + ":user");
		data.getParameters().setString("search_field",org.nrg.xft.XFT.PREFIX + ":user.login");
		data.getParameters().setString("search_value",tempUser.getUsername());
		data.setScreen("UserPermissionsAuthorizationPdf");
	}
	
	
}

