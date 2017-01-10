/*
 * web: org.nrg.xnat.restlet.resources.ProjectGroupResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.*;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.InvalidItemException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class ProjectGroupResource extends SecureResource {
    public static Logger logger = Logger.getLogger(ProjectGroupResource.class);
	public class InvalidValueException extends Exception {

	}

	XnatProjectdata proj=null;
	UserGroupI group=null;
	List<UserI> newUsers= Lists.newArrayList();
	List<String> unknown= Lists.newArrayList();
	String gID=null;
    boolean displayHiddenUsers = false;
	
	public ProjectGroupResource(Context context, Request request, Response response) {
		super(context, request, response);

		this.getVariants().add(new Variant(MediaType.TEXT_XML));
		this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
		this.getVariants().add(new Variant(MediaType.TEXT_HTML));
		
		String pID= (String)getParameter(request,"PROJECT_ID");
		if(pID!=null){
			proj = XnatProjectdata.getProjectByIDorAlias(pID, getUser(), false);
		} 
	
		gID =(String)getParameter(request,"GROUP_ID");
		if(StringUtils.isNotEmpty(gID)){
			if(NumberUtils.isNumber(gID)){
				group=Groups.getGroupByPK(gID);
			}
						
			try {
				if(group==null){
					group=Groups.getGroup(gID);
				}
			} catch (Exception e) {
			}
						
			try {
				if(group==null){
					group=Groups.getGroup(pID + "_" +gID);
				}
			} catch (Exception e) {
			}
						
			if(group==null){
				group=Groups.getGroupByTagAndName(pID,gID);
			}
			

			
		}
	}

	@Override
	public boolean allowPut() {
		return true;
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public boolean allowDelete() {
		return true;
	}
	
	private static List<String> protected_displayNames=Lists.newArrayList("Owners", "Members","Collaborators");
	
	@Override
	public void handleDelete() {
		if(proj==null || group==null){
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return;
		}else if(protected_displayNames.contains(group.getDisplayname())){
			getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			return;
		}else{
			final UserI user = getUser();
			try {
				if(UserHelper.getUserHelperService(user).canDelete(proj)){
					final PersistentWorkflowI workflow=WorkflowUtils.getOrCreateWorkflowData(null, user, XnatProjectdata.SCHEMA_ELEMENT_NAME, proj.getId(),proj.getId(),EventUtils.newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN,EventUtils.TYPE.WEB_SERVICE,"Remove Group"));
		    		final EventMetaI ci=workflow.buildEvent();
		            
					Groups.deleteGroup(group, user, ci);
					
					WorkflowUtils.complete(workflow, ci);
					group=null;
				}else{
					getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
				}
			} catch (InvalidItemException e) {
				logger.error("",e);
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			} catch (Exception e) {
				logger.error("",e);
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		}
		returnDefaultRepresentation();
	}
	
	public void handlePost(){
		handlePut();
	}

	@Override
	public void handlePut() {
		if(proj==null){
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}else{
			try {
				final UserI user = getUser();
				if(Permissions.canDelete(user,proj)){
						boolean isNew = false;				
					Map<String,Object> props = Maps.newHashMap();
					
					props.putAll(getQueryVariablesAsMap());
					
					props.putAll(getBodyVariableMap());
					
					List<PermissionCriteriaI> newPermissions = Lists.newArrayList();
						
					try {
						UserGroupI tempGroup=Groups.createGroup(props);
						
						//tag must be for this project
						if(! StringUtils.equals(proj.getId(), tempGroup.getTag())){
							tempGroup.setTag(proj.getId());
						}
						
						//display name is required
						if(StringUtils.isEmpty(tempGroup.getDisplayname())){
							getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Display name is required.");
							return;
						}
						
						//set ID to the standard value
						if(StringUtils.isEmpty(tempGroup.getId())){
							tempGroup.setId(proj.getId()+"_" + tempGroup.getDisplayname());
						}
						
						
						final List<ElementSecurity> elements = ElementSecurity.GetSecureElements();
						
						for (ElementSecurity es:elements)
						{
							final List<String> permissionItems = es.getPrimarySecurityFields();
							for (String securityField:permissionItems)
							{
								final PermissionCriteria pc = new PermissionCriteria(es.getElementName());
								pc.setField(securityField);
								pc.setFieldValue(tempGroup.getTag());
								final String s = es.getElementName() + "_" + securityField + "_" + tempGroup.getTag();
								if (props.get(s + "_R") != null)
								{
									pc.setRead(true);
								} else {
									pc.setRead(false);
								}
									if (props.get(s + "_E") != null && !StringUtils.equals(es.getElementName(), XnatProjectdata.SCHEMA_ELEMENT_NAME)) {
									pc.setRead(true);
									pc.setEdit(true);
									pc.setCreate(true);
									pc.setActivate(true);
								} else {
									pc.setCreate(false);
									pc.setEdit(false);
									pc.setActivate(false);
								}
									if (props.get(s + "_D") != null && !StringUtils.equals(es.getElementName(), XnatProjectdata.SCHEMA_ELEMENT_NAME)) {
									pc.setRead(true);
									pc.setDelete(true);
								} else {
									pc.setDelete(false);
								}
								
								pc.setComparisonType("equals");
								
								final String wasSet=(String)props.get(s + "_wasSet");
								
								if((wasSet!=null && wasSet.equals("1")) || pc.getCreate() || pc.getRead() || pc.getEdit() || pc.getDelete() || pc.getActivate()){
									newPermissions.add(pc);
																	
									if(StringUtils.equals(pc.getField(), es.getElementName()+"/project")){
										//inherit project permissions to shared project permissions
										if((wasSet!=null && wasSet.equals("1")) || pc.getRead()){

											final PermissionCriteria share = new PermissionCriteria(es.getElementName());
											share.setField(es.getElementName()+"/sharing/share/project");
											share.setFieldValue(tempGroup.getTag());
											share.setRead(pc.getRead());
											share.setComparisonType("equals");
											newPermissions.add(share);
										}
									}
								}
							}
						}
							
						PersistentWorkflowI wrk=PersistentWorkflowUtils.buildOpenWorkflow(user, Groups.getGroupDatatype(), tempGroup.getTag(), tempGroup.getId(), EventUtils.newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS,EventUtils.TYPE.WEB_SERVICE, (group==null)?"Added user group":"Modified user group."));
				         
						if(group==null){
							//need to precreate the group if it doesn't already exist
							Groups.save(tempGroup, user, wrk.buildEvent());
						}
						
						group=tempGroup;
						
						Permissions.setPermissionsForGroup(tempGroup, newPermissions, wrk.buildEvent(),user);

						Groups.save(tempGroup, user, wrk.buildEvent());
						
						PersistentWorkflowUtils.save(wrk, wrk.buildEvent());
						
					} catch (Exception e) {
						logger.error("",e);
						getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e);
						return;
					}

					if(props.containsKey("src")){
						this.getResponse().setStatus(Status.REDIRECTION_SEE_OTHER);
						this.getResponse().redirectSeeOther(XDAT.getSiteConfigPreferences().getSiteUrl() + "/data/projects/" + group.getTag() + "?format=html");
					}else{
						returnDefaultRepresentation();
					}
				}else{
					getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
					return;
				}
			} catch (Exception e) {
				logger.error("",e);
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e);
				return;
			}
		}
	}
	

	@Override
	public Representation represent(Variant variant) {
		if(proj!=null){
			if(group==null){
				//return a list of groups
				try {
	                StringBuffer query = new StringBuffer("SELECT ug.id, ug.displayname,ug.tag,ug.xdat_usergroup_id, COUNT(map.groups_groupid_xdat_user_xdat_user_id) AS users FROM xdat_userGroup ug LEFT JOIN xdat_user_groupid map ON ug.id=map.groupid WHERE tag='").append(proj.getId()).append("' ");
	                query.append(" GROUP BY ug.id, ug.displayname,ug.tag,ug.xdat_usergroup_id  ORDER BY ug.displayname DESC;");
					final UserI user = getUser();
	                final XFTTable table = XFTTable.Execute(query.toString(), user.getDBName(), user.getLogin());
	                
	                Hashtable<String,Object> params= new Hashtable<>();
	        		params.put("title", "Projects");
	        		
	        		if(table!=null) {
	        			params.put("totalRecords", table.size());
					}
	        		return this.representTable(table, overrideVariant(variant), params);
				} catch (SQLException e) {
					logger.error("",e);
					getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
					return null;
				} catch (DBPoolException e) {
					logger.error("",e);
					getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
					return null;
				}
			}else{
				//return a particular group
				return this.representItem(((UserGroup)group).getUserGroupImpl().getItem(), overrideVariant(variant));
			}
		}else{
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
	}
}
