/*
 * org.nrg.xdat.security.UserGroupManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.security;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.nrg.framework.services.NrgEventService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.*;
import org.nrg.xdat.search.CriteriaCollection;
import org.nrg.xdat.security.group.exceptions.GroupFieldMappingException;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTTable;
import org.nrg.xft.cache.CacheManager;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.XftStringUtils;

import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class UserGroupManager implements UserGroupServiceI{

	static Logger logger = Logger.getLogger(UserGroupManager.class);
    
	
    public UserGroupI getGroup(String id){
    	if(id==null){
    		return null;
    	}
    	
    	//reintroduce caching as on-demand 11/09 TO
    	UserGroup g =(UserGroup) CacheManager.GetInstance().retrieve(XdatUsergroup.SCHEMA_ELEMENT_NAME, id);
    	if(g==null){
    		try {
                XdatUsergroup temp = XdatUsergroup.getXdatUsergroupsById(id, null, true);
                if(temp!=null){
                    g = new UserGroup(temp);
                    CacheManager.GetInstance().put(XdatUsergroup.SCHEMA_ELEMENT_NAME, id, g);
                }
                return g;
            } catch (Throwable e) {
                logger.error("",e);
                return null;
            }
    	}else{
    		return g;
    	}
    }


	@Override
	public boolean isMember(UserI user, String grp) {
		return getGroupIdsForUser(user).contains(grp);
	}


	@Override
	public Map<String, UserGroupI> getGroupsForUser(UserI user) {
		return ((XDATUser)user).getGroups();
	}


	@Override
	public List<String> getGroupIdsForUser(UserI user) {
		List<String> grps=Lists.newArrayList();
		for (XdatUserGroupid g : ((XDATUser)user).getGroups_groupid()) {
            grps.add(g.getGroupid());
        }
		return grps;
	}


	@Override
	public void updateUserForGroup(UserI user, String groupId, UserGroupI group) {
	    ((XDATUser)user).getGroups().put(groupId,group);
	    ((XDATUser)user).resetCriteria();
	}

	@Override
	public void removeUserFromGroup(UserI user, String groupId, EventMetaI ci) throws Exception {
		for (XdatUserGroupid map : ((XDATUser)user).getGroups_groupid()) {
			if (map.getGroupid().equals(groupId)) {
				SaveItemHelper.authorizedDelete(map.getItem(), user,ci);
			}
		}
	    ((XDATUser)user).resetCriteria();
	}

	@Override
	public void reloadGroupForUser(UserI user, String groupId) {
		((XDATUser)user).refreshGroup(groupId);
		
	}

	@Override
	public void reloadGroupsForUser(UserI user) {
		((XDATUser)user).initGroups();
	}
	
	private void initPermissions(UserGroupI group, Boolean create,Boolean read,Boolean delete,Boolean edit,Boolean activate,List<ElementSecurity> ess, String value, UserI authenticatedUser){
		String templateName="new_group_permissions.vm";
        try {
	        boolean velocityInit = false;

	        try {
                Velocity.resourceExists(templateName);
                velocityInit=true;
            } catch (Exception ignored) {
            }

            if (velocityInit)
            {
                boolean exists= Velocity.resourceExists("/screens/" + templateName);
                if (exists)
                {
                    VelocityContext context = new VelocityContext();
                    context.put("group", group);
                    context.put("create", (create)?"1":"0");
                    context.put("read", (read)?"1":"0");
                    context.put("delete", (delete)?"1":"0");
                    context.put("edit", (edit)?"1":"0");
                    context.put("activate", (activate)?"1":"0");
                    context.put("ess", ess);
                    context.put("value", value);
                    context.put("user", authenticatedUser);
                    StringWriter sw = new StringWriter();
                    Template template =Velocity.getTemplate("/screens/" + templateName);
                    template.merge(context,sw);
                    
                    ArrayList<String> stmts= XftStringUtils.DelimitedStringToArrayList(sw.toString(), ";");
                    PoolDBUtils.ExecuteBatch(stmts, null, authenticatedUser.getUsername());
                }
            }
        } catch (Exception ignored) {
        }
	}
	

    public UserGroupI createGroup(final String id, final String displayName, Boolean create,Boolean read,Boolean delete,Boolean edit,Boolean activate,boolean activateChanges,List<ElementSecurity> ess, String value, UserI authenticatedUser){
    	XdatUsergroup group;
    	PersistentWorkflowI wrk=null;
    	
    	try {
            group = new XdatUsergroup(authenticatedUser);
            group.setId(id);
            group.setDisplayname(displayName);
            group.setTag(value);

            UserGroupI existing=Groups.getGroup(id);
            
            if(existing==null){
            	//optimized version for expediency
            	wrk=PersistentWorkflowUtils.buildOpenWorkflow(authenticatedUser, group.getXSIType(), "ADMIN", value, EventUtils.newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.TYPE.PROCESS, "Initialized permissions"));

                assert wrk != null;
                SaveItemHelper.authorizedSave(group, authenticatedUser,false,false,wrk.buildEvent());
        		
        		existing=Groups.getGroup(id);
        		
        		initPermissions(existing, create, read, delete, edit, activate, ess, value, authenticatedUser);
        		
        		wrk.setId(existing.getPK().toString());
        		
        		
        		
    			PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());
    			
    	        try {
    				DBAction.InsertMetaDatas(XdatElementAccess.SCHEMA_ELEMENT_NAME);
    				DBAction.InsertMetaDatas(XdatFieldMappingSet.SCHEMA_ELEMENT_NAME);
    				DBAction.InsertMetaDatas(XdatFieldMapping.SCHEMA_ELEMENT_NAME);
    			} catch (Exception e2) {
    	            logger.error("",e2);
    			}
    	        
    	        try {
    	        	final NrgEventService eventService = XDAT.getContextService().getBean(NrgEventService.class);
    	        	eventService.triggerEvent(new XftItemEvent(Groups.getGroupDatatype(),existing.getId(),XftItemEvent.UPDATE));
    			} catch (Exception e1) {
    	            logger.error("",e1);
    			}
    			
    			try {
    				PoolDBUtils.ClearCache(null, authenticatedUser.getUsername(), Groups.getGroupDatatype());
    			} catch (Exception e) {
    	            logger.error("",e);
    			}
    			
    			return Groups.getGroup(id);
            } else{
            	throw new Exception("Group already exists");
            }
        } catch (Exception e) {
            logger.error("",e);
            try {
				if(wrk!=null) PersistentWorkflowUtils.fail(wrk, wrk.buildEvent());
			} catch (Exception ignored) {
			}
            return null;
        }
    }

    public UserGroupI createOrUpdateGroup(final String id, final String displayName, Boolean create,Boolean read,Boolean delete,Boolean edit,Boolean activate,boolean activateChanges,List<ElementSecurity> ess, String value, UserI authenticatedUser) throws Exception{
    	
    	//hijacking the code her to manually create a group if it is brand new.  Should make project creation much quicker.
    	Long matches=(Long)PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(ID) FROM xdat_usergroup WHERE ID='" + id + "'", "COUNT", authenticatedUser.getDBName(), null);
    	if(matches==0){
        	//this means the group is new.  It doesn't need to be as thorough as an edit of an existing one would be.
    		return createGroup(id,displayName,create,read,delete,edit,activate,activateChanges,ess,value,authenticatedUser);
    	}
    	
    	PersistentWorkflowI wrk;
    	XdatUsergroup group;
    	
    	//this means the group previously existed, and this is an update rather than an init.
    	//the logic here will be way more intrusive (and expensive)
    	//it will end up checking every individual permission setting (using very inefficient code)
    	final ArrayList<XdatUsergroup> groups = XdatUsergroup.getXdatUsergroupsByField(XdatUsergroup.SCHEMA_ELEMENT_NAME +".ID", id, authenticatedUser, true);
    	boolean modified=false;

        if (groups.size()==0){
        	throw new Exception("Count didn't match query results");
        }else{
            group = groups.get(0);
            wrk=PersistentWorkflowUtils.buildOpenWorkflow(authenticatedUser, group.getXSIType(), group.getXdatUsergroupId().toString(), value, EventUtils.newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.TYPE.PROCESS, "Modified permissions"));
        }

    	long start=Calendar.getInstance().getTimeInMillis();
        try {
        	if(group.getDisplayname().equals("Owners")){
                assert wrk != null;
                setPermissions(group,"xnat:projectData", "xnat:projectData/ID", value, create,read,delete,edit,activate,activateChanges, authenticatedUser,false,wrk.buildEvent());
        	}else{
                assert wrk != null;
                setPermissions(group,"xnat:projectData", "xnat:projectData/ID", value, Boolean.FALSE,read,Boolean.FALSE,Boolean.FALSE,Boolean.FALSE,activateChanges,authenticatedUser,false,wrk.buildEvent());
        	}

            for (final ElementSecurity es : ess) {
                if (setPermissions(group, es.getElementName(), es.getElementName() + "/project", value, create, read, delete, edit, activate, activateChanges, authenticatedUser, false, wrk.buildEvent())) {
                    modified = true;
                }

                if (setPermissions(group, es.getElementName(), es.getElementName() + "/sharing/share/project", value, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, authenticatedUser, false, wrk.buildEvent())) {
                    modified = true;
                }
            }
        } catch (Exception e) {
            if(wrk!=null) PersistentWorkflowUtils.fail(wrk, wrk.buildEvent());
            logger.error("",e);
        }


        try {
			if(modified){
				PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());
    	        
    	        try {
    	        	final NrgEventService eventService = XDAT.getContextService().getBean(NrgEventService.class);
    	        	eventService.triggerEvent(new XftItemEvent(Groups.getGroupDatatype(),group.getId(),XftItemEvent.UPDATE));
    			} catch (Exception e1) {
    	            logger.error("",e1);
    			}
    			
    			try {
    				PoolDBUtils.ClearCache(null, authenticatedUser.getUsername(), Groups.getGroupDatatype());
    			} catch (Exception e) {
    	            logger.error("",e);
    			}

			}
		} catch (Exception e1) {
            PersistentWorkflowUtils.fail(wrk, wrk.buildEvent());
            logger.error("",e1);
		}

        logger.debug( "UPDATE GROUP "+id + "... "+(Calendar.getInstance().getTimeInMillis()-start)+ " ms");
        
		return new UserGroup(group);
    }


	@Override
	public List<UserGroupI> getGroupsByTag(String tag) throws Exception {
		List<UserGroupI> ug=Lists.newArrayList();
		
		final CriteriaCollection col = new CriteriaCollection("AND");
        col.addClause(XdatUsergroup.SCHEMA_ELEMENT_NAME +".tag","=", "'" + tag + "'",true);
        for(XdatUsergroup gp:XdatUsergroup.getXdatUsergroupsByField(col, null, false)){
        	ug.add(new UserGroup(gp));
        }

        return ug;
	}

	@Override
	public UserGroupI addUserToGroup(String group_id, UserI newUser,UserI currentUser, EventMetaI ci) throws Exception{
    	UserGroupI gp=Groups.getGroup(group_id);
    	
    	if(gp.getTag()!=null){
	    	//remove from existing groups
			for (Map.Entry<String, UserGroupI> entry : Groups.getGroupsForUser(newUser).entrySet()) {
				if (entry.getValue().getTag()!=null && entry.getValue().getTag().equals(gp.getTag())) {
					if(entry.getValue().getId().equals(group_id)){
						return gp;
					}
	
					//find mapping object to delete
					if(Groups.isMember(newUser, entry.getValue().getId())){
						Groups.removeUserFromGroup(newUser, entry.getValue().getId(),ci);
					}
				}
			}
    	}

    	final String confirmquery = "SELECT * FROM xdat_user_groupid WHERE groupid='" + group_id + "' AND groups_groupid_xdat_user_xdat_user_id=" + newUser.getID() + ";";

        XFTTable t = XFTTable.Execute(confirmquery, newUser.getDBName(), newUser.getUsername());
        if (t.size() == 0) {
            final XdatUserGroupid map = new XdatUserGroupid(currentUser);
            map.setProperty(map.getXSIType() + ".groups_groupid_xdat_user_xdat_user_id", newUser.getID());
            map.setGroupid(group_id);
            SaveItemHelper.authorizedSave(map, currentUser, false, false, ci);
        }

        return gp;
	}


    private boolean setPermissions(XdatUsergroup impl, String elementName, String psf, String value, Boolean create, Boolean read, Boolean delete, Boolean edit, Boolean activate, boolean activateChanges, UserI user, boolean includesModification, EventMetaI c) throws Exception {
        try {

            XdatElementAccess ea = null;
            for (XdatElementAccess temp : impl.getElementAccess()) {
                if (temp.getElementName().equals(elementName)) {
                    ea = temp;
                    break;
                }
            }

            if (ea == null) {
                ea = new XdatElementAccess(user);
                ea.setElementName(elementName);
                ea.setProperty("xdat_usergroup_xdat_usergroup_id", impl.getXdatUsergroupId());
            }

            final XdatFieldMappingSet fms;
            ArrayList al = ea.getPermissions_allowSet();
            if (al.size() > 0) {
                fms = ea.getPermissions_allowSet().get(0);
            } else {
                fms = new XdatFieldMappingSet(user);
                fms.setMethod("OR");
                ea.setPermissions_allowSet(fms);
            }

            XdatFieldMapping fm = null;

            for (final XdatFieldMapping mapping : fms.getAllow()) {
                if (mapping.getFieldValue().equals(value) && mapping.getField().equals(psf)) {
                    fm = mapping;
                    break;
                }
            }

            if (fm == null) {
                if (create || read || edit || delete || activate)
                    fm = new XdatFieldMapping(user);
                else
                    return false;
            } else if (!includesModification) {
                if (!(create || read || edit || delete || activate)) {
                    if (fms.getAllow().size() == 1) {
                        SaveItemHelper.authorizedDelete(fms.getItem(), user, c);
                        return true;
                    } else {
                        SaveItemHelper.authorizedDelete(fm.getItem(), user, c);
                        return true;
                    }
                }
                return false;
            }

            fm.init(psf, value, create, read, delete, edit, activate);

            fms.setAllow(fm);

            if (fms.getXdatFieldMappingSetId() != null) {
                fm.setProperty("xdat_field_mapping_set_xdat_field_mapping_set_id", fms.getXdatFieldMappingSetId());

                if (activateChanges) {
                    SaveItemHelper.authorizedSave(fm, user, true, false, true, false, c);
                    fm.activate(user);
                } else {
                    SaveItemHelper.authorizedSave(fm, user, true, false, false, false, c);
                }
            } else if (ea.getXdatElementAccessId() != null) {
                fms.setProperty("permissions_allow_set_xdat_elem_xdat_element_access_id", ea.getXdatElementAccessId());
                if (activateChanges) {
                    SaveItemHelper.authorizedSave(fms, user, true, false, true, false, c);
                    fms.activate(user);
                } else {
                    SaveItemHelper.authorizedSave(fms, user, true, false, false, false, c);
                }
            } else {
                if (activateChanges) {
                    SaveItemHelper.authorizedSave(ea, user, true, false, true, false, c);
                    ea.activate(user);
                } else {
                    SaveItemHelper.authorizedSave(ea, user, true, false, false, false, c);
                }
                impl.setElementAccess(ea);
            }
        } catch (Exception e) {
            logger.error("", e);
        }

        return true;
    }


    @Override
	public List<UserGroupI> getAllGroups() {
		List<UserGroupI> groups=Lists.newArrayList();
		
		try {
			XFTTable t=XFTTable.Execute("SELECT xdat_userGroup_id, ID, displayname, tag FROM xdat_userGroup;", null, null);
			t.resetRowCursor();
			while(t.hasMoreRows()){
				Object[] row=t.nextRow();
				UserGroup g=new UserGroup();
				g.setPK((Integer)row[0]);
				g.setId((String)row[1]);
				g.setDisplayname((String)row[2]);
				g.setTag((String)row[3]);
				groups.add(g);
			}
		} catch (SQLException | DBPoolException e) {
			logger.error("",e);
		}
        return groups;
	}


	@Override
	public String getGroupDatatype() {
		return XdatUsergroup.SCHEMA_ELEMENT_NAME;
	}


	@Override
	public void deleteGroupsByTag(String tag, UserI user, EventMetaI ci) throws Exception {
		for(UserGroupI g: getGroupsByTag(tag)){
			deleteGroup(g, user, ci);
		}
	}


	@Override
	public UserGroupI createGroup(Map<String, ?> params) throws GroupFieldMappingException {
		try {
			PopulateItem populater = new PopulateItem(params,null,XdatUsergroup.SCHEMA_ELEMENT_NAME,true);
			ItemI found = populater.getItem();
			return new UserGroup(new XdatUsergroup(found));
		} catch (Exception e) {
			throw new GroupFieldMappingException(e);
		}
	}


	@Override
	public void deleteGroup(UserGroupI group, UserI user, EventMetaI ci) {
      //DELETE user.groupId
        CriteriaCollection col = new CriteriaCollection("AND");
        col.addClause(XdatUserGroupid.SCHEMA_ELEMENT_NAME +".groupid", " = ", group.getId());

        for (final XdatUserGroupid gId : XdatUserGroupid.getXdatUserGroupidsByField(col, user, false)) {
            try {
                SaveItemHelper.authorizedDelete(gId.getItem(), user, ci);
            } catch (Throwable e) {
                logger.error("", e);
            }
        }
        
        try {
        	XdatUsergroup tmp=XdatUsergroup.getXdatUsergroupsByXdatUsergroupId(group.getPK(), user, false);

            assert tmp != null;
            SaveItemHelper.authorizedDelete(tmp.getItem(), user,ci);
        } catch (Throwable e) {
            logger.error("",e);
        }
        


        try {
 	       	final NrgEventService eventService = XDAT.getContextService().getBean(NrgEventService.class);
        	eventService.triggerEvent(new XftItemEvent(Groups.getGroupDatatype(), group.getId(), XftItemEvent.DELETE));
		} catch (Exception e1) {
            logger.error("",e1);
		}
		
		try {
			PoolDBUtils.ClearCache(null, user.getUsername(), Groups.getGroupDatatype());
		} catch (Exception e) {
            logger.error("",e);
		}
	}


	@Override
	public UserGroupI getGroupByPK(Object gID) {
		try {
			String id=(String)PoolDBUtils.ReturnStatisticQuery(String.format("SELECT id FROM xdat_usergroup WHERE xdat_userGroup_id=%1$s;",gID), "id", null, null);
			if(id!=null){
				return getGroup(id);
			}
		} catch (Exception e) {
			logger.error("",e);
		}
		return null;
	}


	public void validateGroupByTag(XdatUsergroup tempGroup, String tag) throws InvalidValueException{
		//verify that the user isn't trying to gain access to other projects.
		for(XdatElementAccess ea:tempGroup.getElementAccess()){
			for(XdatFieldMappingSet set: ea.getPermissions_allowSet()){
				List<String> values=getPermissionValues(set);
				if(values.size()!=1){
					throw new InvalidValueException();
				}
				if(!StringUtils.equals(values.get(0), tag)){
					throw new InvalidValueException();
				}
			}
		}
	}
	

	
	private List<String> getPermissionValues(XdatFieldMappingSet set) {
		List<String> values=Lists.newArrayList();
		
		for(final XdatFieldMapping map: set.getAllow()){
			if(!values.contains(map.getFieldValue())){
				values.add(map.getFieldValue());
			}
		}
		
		for(XdatFieldMappingSet subset: set.getSubSet()){
			values.addAll(getPermissionValues(subset));
		}
		
		return values;
	}


	@Override
	public UserGroupI getGroupByTagAndName(String pID, String gID) {
		try {
			String id=(String)PoolDBUtils.ReturnStatisticQuery(String.format("SELECT id FROM xdat_usergroup WHERE tag='%1$s' AND displayname='%2$s';",pID,gID), "id", null, null);
			if(id!=null){
				return getGroup(id);
			}
		} catch (Exception e) {
			logger.error("",e);
		}
		return null;
	}


	@Override
	public void save(UserGroupI group, UserI user, EventMetaI meta) throws Exception{
		if(((UserGroup)group).xdatGroup==null){
			return;
		}
		
		XdatUsergroup xdatGroup=((UserGroup)group).xdatGroup;
		
		if(!Roles.isSiteAdmin(user)){
			String firstValue=null;
			for(XdatElementAccess ea:xdatGroup.getElementAccess()){
                List<String> values=getPermissionValues(ea.getPermissions_allowSet().get(0));
                firstValue=values.get(0);
				if(firstValue!=null){
					break;
				}
			}
			
			validateGroupByTag(xdatGroup, firstValue);

            final UserHelperServiceI userHelperService = UserHelper.getUserHelperService(user);
            if(userHelperService != null && !userHelperService.isOwner(firstValue)){
				throw new InvalidValueException();
			}
		}
		
		SaveItemHelper.authorizedSave(xdatGroup, user, false,true, meta);
		
		((UserGroup)group).init(xdatGroup.getItem());
		((UserGroup)group).xdatGroup=null;
		
		try {
 	       	final NrgEventService eventService = XDAT.getContextService().getBean(NrgEventService.class);
			eventService.triggerEvent(new XftItemEvent(XdatUsergroup.SCHEMA_ELEMENT_NAME,group.getId(),XftItemEvent.UPDATE));
			Groups.reloadGroupsForUser(user);
		} catch (Exception e1) {
			logger.error("", e1);
		}
	}


}
