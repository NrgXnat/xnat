//Copyright 2007 Washington University School of Medicine All Rights Reserved
/*
 * Created on Jun 29, 2007
 *
 */
package org.nrg.xdat.turbine.modules.actions;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.PermissionCriteria;
import org.nrg.xdat.security.PermissionItem;
import org.nrg.xdat.security.UserGroup;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.Event;
import org.nrg.xft.event.EventManager;
public class ModifyGroupPrivileges extends SecureAction {
	static Logger logger = Logger.getLogger(ModifyUserPrivileges.class);
	public void doPerform(RunData data, Context context) throws Exception
	{
		final XdatUsergroup tempUser = storeChanges(data, context);
		TurbineUtils.setDataItem(data, tempUser.getItem());
		data.getParameters().setString("search_element",
				org.nrg.xft.XFT.PREFIX + ":userGroup");
		data.getParameters().setString("search_field",
				org.nrg.xft.XFT.PREFIX + ":userGroup.ID");
		data.getParameters().setString("search_value", tempUser.getId());
		data.setAction("DisplayItemAction");
		VelocityAction action = (VelocityAction) ActionLoader.getInstance()
				.getInstance("DisplayItemAction");
		action.doPerform(data, context);
		ElementSecurity.refresh();
	}
	/**
	 * 
	 * @param tempUser
	 * 
	 * @param props
	 * 
	 * @return
	 * 
	 * @throws Exception
	 * 
	 */
	public static XdatUsergroup SetGroupProperties(XdatUsergroup tempUser,
			Map<String,? extends Object> props, String userName) throws Exception
	{
		final List<ElementSecurity> elements = ElementSecurity.GetSecureElements();
		
		final UserGroup ug = new UserGroup(tempUser.getId());
		ug.init(tempUser);
		for (ElementSecurity es:elements)
		{
			final List<PermissionItem> permissionItems = es.getPermissionItems(userName);
			for (PermissionItem pi:permissionItems)
			{
				final PermissionCriteria pc = new PermissionCriteria();
				pc.setField(pi.getFullFieldName());
				pc.setFieldValue(pi.getValue());
				final String s = es.getElementName() + "_" + pi.getFullFieldName()
						+ "_" + pi.getValue();
				if (props.get(s.toLowerCase() + "_r") != null)
				{
					pc.setRead(true);
				} else {
					pc.setRead(false);
				}
				if (props.get(s.toLowerCase() + "_c") != null)
				{
					pc.setCreate(true);
				} else {
					pc.setCreate(false);
				}
				if (props.get(s.toLowerCase() + "_e") != null)
				{
					pc.setEdit(true);
				} else {
					pc.setEdit(false);
				}
				if (props.get(s.toLowerCase() + "_d") != null)
				{
					pc.setDelete(true);
				} else {
					pc.setDelete(false);
				}
				if (props.get(s.toLowerCase() + "_a") != null)
				{
					pc.setActivate(true);
				} else {
					pc.setActivate(false);
				}
				if (props.get(s.toLowerCase() + "_type") != null)
				{
					pc.setComparisonType((String) props.get(s.toLowerCase()
							+ "_type"));
				}
				
				final String wasSet=(String)props.get(s.toLowerCase() + "_wasSet");
				
				if(wasSet.equals("1") || pc.getCreate() || pc.getRead() || pc.getEdit() || pc.getDelete() || pc.getActivate()){
					tempUser.addRootPermission(es.getElementName(),pc);
				}
			}
		}
		return tempUser;
	}
	public XdatUsergroup storeChanges(RunData data, Context context)
			throws Exception
	{
		// TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
		// parameter specifying elementAliass and elementNames
		PopulateItem populater = PopulateItem.Populate(data, "xdat:userGroup",
				true);
		final ItemI found = populater.getItem();
		XdatUsergroup tempGroup = new XdatUsergroup(found);
		try {
			tempGroup.getItem().save(TurbineUtils.getUser(data), false, false);
		} catch (Exception e) {
			logger.error("Error Storing User", e);
		}
		tempGroup = new XdatUsergroup(found.getCurrentDBVersion());
		// logger.error("3\n"+tempUser.getItem().toString());
		final Map<String,String> props = TurbineUtils.GetDataParameterHash(data);
		tempGroup = SetGroupProperties(tempGroup, props, TurbineUtils.getUser(
				data).getLogin());
		// logger.error("4\n"+tempUser.getItem().toString());
		try {
			tempGroup.getItem().save(TurbineUtils.getUser(data), true, false);
			// temp = tempUser.getItem().getCurrentDBVersion();
			// tempUser = new XDATUser(temp);
		} catch (Exception e) {
			logger.error("Error Storing Group", e);
		}
		try {
			EventManager.Trigger(XdatUsergroup.SCHEMA_ELEMENT_NAME, tempGroup
					.getId(), Event.UPDATE);
		} catch (Exception e1) {
			logger.error("", e1);
		}
		TurbineUtils.getUser(data).init();
		return tempGroup;
	}
}
