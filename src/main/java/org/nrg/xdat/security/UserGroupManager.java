//Copyright 2007 Washington University School of Medicine All Rights Reserved
/*
 * Created on Jun 29, 2007
 *
 */
package org.nrg.xdat.security;

import java.io.File;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.nrg.xdat.om.XdatElementAccess;
import org.nrg.xdat.om.XdatFieldMapping;
import org.nrg.xdat.om.XdatFieldMappingSet;
import org.nrg.xdat.om.XdatUserGroupid;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.search.CriteriaCollection;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.cache.CacheManager;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.Event;
import org.nrg.xft.event.EventManager;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.StringUtils;
import org.nrg.xft.utils.VelocityUtils;

import com.google.common.collect.Lists;

public class UserGroupManager implements UserGroupServiceI{
    static Logger logger = Logger.getLogger(UserGroupManager.class);
    
	
    public UserGroupI getGroup(String id){
    	//reintroduce caching as on-demand 11/09 TO
    	UserGroup g =(UserGroup) CacheManager.GetInstance().retrieve(XdatUsergroup.SCHEMA_ELEMENT_NAME, id);
    	if(g==null){
    		try {
                XdatUsergroup temp =(XdatUsergroup) XdatUsergroup.getXdatUsergroupsById(id, null, true);
                if(temp!=null){
                    g = new UserGroup(temp);
                    if(g!=null)CacheManager.GetInstance().put(XdatUsergroup.SCHEMA_ELEMENT_NAME, id, g);
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
	public void removeUserFromGroup(UserI user, String groupId, EventMetaI ci) throws SQLException, Exception {
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
                Velocity.templateExists(templateName);
                velocityInit=true;
            } catch (Exception e1) {
            }

            if (velocityInit)
            {
                boolean exists= Velocity.templateExists("/screens/" + templateName);
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
                    
                    ArrayList<String> stmts=StringUtils.DelimitedStringToArrayList(sw.toString(), ";");
                    PoolDBUtils.ExecuteBatch(stmts, null, authenticatedUser.getUsername());
                }
            }
        } catch (Exception e) {
        }
	}
	

    public UserGroupI createGroup(final String id, final String displayName, Boolean create,Boolean read,Boolean delete,Boolean edit,Boolean activate,boolean activateChanges,List<ElementSecurity> ess, String value, UserI authenticatedUser){
    	XdatUsergroup group=null;
    	PersistentWorkflowI wrk=null;
    	
    	try {
        	long start=Calendar.getInstance().getTimeInMillis();
            group = new XdatUsergroup(authenticatedUser);
            group.setId(id);
            group.setDisplayname(displayName);
            group.setTag(value);

            UserGroupI existing=Groups.getGroup(id);
            
            if(existing==null){
            	//optimized version for expediency
            	wrk=PersistentWorkflowUtils.buildOpenWorkflow(authenticatedUser, group.getXSIType(), "ADMIN", value, EventUtils.newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.TYPE.PROCESS, "Initialized permissions"));
                 
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
    				EventManager.Trigger(Groups.getGroupDatatype(),existing.getId(),Event.UPDATE);
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
			} catch (Exception e1) {
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
    	
    	PersistentWorkflowI wrk=null;
    	XdatUsergroup group=null;
    	
    	//this means the group previously existed, and this is an update rather than an init.
    	//the logic here will be way more intrusive (and expensive)
    	//it will end up checking every individual permission setting (using very inefficient code)
    	final ArrayList<XdatUsergroup> groups = XdatUsergroup.getXdatUsergroupsByField(XdatUsergroup.SCHEMA_ELEMENT_NAME +".ID", id, authenticatedUser, true);
    	boolean modified=false;

        if (groups.size()==0){
        	throw new Exception("Count didn't match query results");
        }else{
            group = (XdatUsergroup)groups.get(0);
            wrk=PersistentWorkflowUtils.buildOpenWorkflow(authenticatedUser, group.getXSIType(), group.getXdatUsergroupId().toString(), value, EventUtils.newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.TYPE.PROCESS, "Modified permissions"));
        }

    	long start=Calendar.getInstance().getTimeInMillis();
        try {
        	if(group.getDisplayname().equals("Owners")){
        		setPermissions(group,"xnat:projectData", "xnat:projectData/ID", value, create,read,delete,edit,activate,activateChanges, authenticatedUser,false,wrk.buildEvent());
        	}else{
        		setPermissions(group,"xnat:projectData", "xnat:projectData/ID", value, Boolean.FALSE,read,Boolean.FALSE,Boolean.FALSE,Boolean.FALSE,activateChanges,authenticatedUser,false,wrk.buildEvent());
        	}

            Iterator iter = ess.iterator();
            while (iter.hasNext())
            {
                ElementSecurity es = (ElementSecurity)iter.next();


                if(setPermissions(group,es.getElementName(),es.getElementName() + "/project", value, create,read,delete,edit,activate,activateChanges, authenticatedUser,false,wrk.buildEvent())){
                	modified=true;
                }

                if(setPermissions(group,es.getElementName(),es.getElementName() + "/sharing/share/project", value, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, authenticatedUser,false,wrk.buildEvent())){
                	modified=true;
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
    				EventManager.Trigger(Groups.getGroupDatatype(),group.getId(),Event.UPDATE);
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
            if(wrk!=null) PersistentWorkflowUtils.fail(wrk, wrk.buildEvent());
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
	public UserGroupI addUserToGroup(String group_id, String tag, UserI newUser,UserI currentUser, EventMetaI ci) throws Exception{
    	boolean isOwner=false;
    	UserGroupI gp=Groups.getGroup(group_id);

    	//remove fom existing groups
		for (Map.Entry<String, UserGroupI> entry : Groups.getGroupsForUser(newUser).entrySet()) {
			if (entry.getValue().getTag()!=null && entry.getValue().getTag().equals(tag)) {
				if(entry.getValue().getId().equals(group_id)){
					return gp;
				}

				//find mapping object to delete
				if(Groups.isMember(newUser, entry.getValue().getId())){
					Groups.removeUserFromGroup(newUser, entry.getValue().getId(),ci);
				}
			}
		}

    	final String confirmquery = "SELECT * FROM xdat_user_groupid WHERE groupid='" + group_id + "' AND groups_groupid_xdat_user_xdat_user_id=" + newUser.getID() + ";";

    	if(!isOwner){
			XFTTable t=XFTTable.Execute(confirmquery,newUser.getDBName(), newUser.getUsername());
	    	if(t.size()==0){
	            final XdatUserGroupid map = new XdatUserGroupid((UserI)currentUser);
	            map.setProperty(map.getXSIType() +".groups_groupid_xdat_user_xdat_user_id", newUser.getID());
	            map.setGroupid(group_id);
	            SaveItemHelper.authorizedSave(map,currentUser, false, false,ci);
	    	}
    	}
    	    	
        return gp;
	}
	

	private boolean setPermissions(XdatUsergroup impl, String elementName,	String psf, String value, Boolean create, Boolean read,	Boolean delete, Boolean edit, Boolean activate,	boolean activateChanges, UserI user, boolean includesModification, EventMetaI c) throws Exception {
		try {
            final ElementSecurity es = ElementSecurity.GetElementSecurity(elementName);
            
                XdatElementAccess ea = null;
                for (XdatElementAccess temp:impl.getElementAccess())
                {
                    if(temp.getElementName().equals(elementName))
                    {
                        ea= temp;
                        break;
                    }
                }

                if (ea==null)
                {
                    ea = new XdatElementAccess((UserI)user);
                    ea.setElementName(elementName);
                    ea.setProperty("xdat_usergroup_xdat_usergroup_id", impl.getXdatUsergroupId());
                }

                XdatFieldMappingSet fms = null;
                ArrayList al =  ea.getPermissions_allowSet();
                if (al.size()>0){
                    fms = (XdatFieldMappingSet)ea.getPermissions_allowSet().get(0);
                }else{
                    fms = new XdatFieldMappingSet((UserI)user);
                    fms.setMethod("OR");
                    ea.setPermissions_allowSet(fms);
                }

                XdatFieldMapping fm = null;

                Iterator iter = fms.getAllow().iterator();
                while (iter.hasNext())
                {
                    Object o = iter.next();
                    if (o instanceof XdatFieldMapping)
                    {
                        if (((XdatFieldMapping)o).getFieldValue().equals(value) && ((XdatFieldMapping)o).getField().equals(psf)){
                            fm = (XdatFieldMapping)o;
                        }
                    }
                }

                if (fm ==null){
                	if(create || read || edit || delete || activate)
                		fm = new XdatFieldMapping((UserI)user);
                	else
                		return false;
                }else if(!includesModification){
                	if(!(create || read || edit || delete || activate)){
                		if(fms.getAllow().size()==1){
                			SaveItemHelper.authorizedDelete(fms.getItem(), user,c);
                			return true;
                		}else{
                			SaveItemHelper.authorizedDelete(fm.getItem(), user,c);
                			return true;
                		}
                	}
                    return false;
                }

                fm.init(psf, value, create, read, delete, edit, activate);
                
                fms.setAllow(fm);

                if (fms.getXdatFieldMappingSetId()!=null)
                {
                    fm.setProperty("xdat_field_mapping_set_xdat_field_mapping_set_id", fms.getXdatFieldMappingSetId());

                    if (activateChanges){
                    	SaveItemHelper.authorizedSave(fm,user, true, false, true, false,c);
                        fm.activate(user);
                    }else{
                    	SaveItemHelper.authorizedSave(fm,user, true, false, false, false,c);
                    }
                }else if(ea.getXdatElementAccessId()!=null){
                    fms.setProperty("permissions_allow_set_xdat_elem_xdat_element_access_id", ea.getXdatElementAccessId());
                    if (activateChanges){
                    	SaveItemHelper.authorizedSave(fms,user, true, false, true, false,c);
                        fms.activate(user);
                    }else{
                    	SaveItemHelper.authorizedSave(fms,user, true, false, false, false,c);
                    }
                }else{
                    if (activateChanges){
                    	SaveItemHelper.authorizedSave(ea,user, true, false, true, false,c);
                        ea.activate(user);
                    }else{
                    	SaveItemHelper.authorizedSave(ea,user, true, false, false, false,c);
                    }
                    impl.setElementAccess(ea);
                }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        } catch (InvalidValueException e) {
            logger.error("",e);
        } catch (Exception e) {
            logger.error("",e);
        }

        return true;
	}
	

    
    private void init(XdatUsergroup group, String elementName, String value,Boolean create,Boolean read,Boolean delete,Boolean edit,Boolean activate, UserI authenticatedUser) throws Exception{
    	XdatElementAccess ea=new XdatElementAccess(authenticatedUser);
        ea.setElementName(elementName);
        group.setElementAccess(ea);
        
        //container for field mapping settings
        XdatFieldMappingSet fms = new XdatFieldMappingSet(authenticatedUser);
        fms.setMethod("OR");
        ea.setPermissions_allowSet(fms);
                            
        //access permissions for owned data
        XdatFieldMapping fm= new XdatFieldMapping(authenticatedUser);
        fm.init( elementName + "/project", value, create,read,delete,edit,activate);
        fms.setAllow(fm);

        //access permissions for shared data
        fm= new XdatFieldMapping(authenticatedUser);
        fm.init( elementName + "/sharing/share/project", value, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE);
        fms.setAllow(fm);
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
		} catch (SQLException e) {
			logger.error("",e);
		} catch (DBPoolException e) {
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
        //DELETE user.groupId
        CriteriaCollection col = new CriteriaCollection("AND");
        col.addClause(XdatUserGroupid.SCHEMA_ELEMENT_NAME +".groupid"," SIMILAR TO ", tag + "\\_(owner|member|collaborator)");
        Iterator groups = XdatUserGroupid.getXdatUserGroupidsByField(col, user, false).iterator();

        while(groups.hasNext()){
            XdatUserGroupid g = (XdatUserGroupid)groups.next();
            try {
            	SaveItemHelper.authorizedDelete(g.getItem(), user,ci);
            } catch (Throwable e) {
                logger.error("",e);
            }
        }

        //DELETE user groups
        col = new CriteriaCollection("AND");
        col.addClause(XdatUsergroup.SCHEMA_ELEMENT_NAME +".ID"," SIMILAR TO ", tag + "\\_(owner|member|collaborator)");
        groups = XdatUsergroup.getXdatUsergroupsByField(col, user, false).iterator();

        while(groups.hasNext()){
            XdatUsergroup g = (XdatUsergroup)groups.next();
            try {
            	SaveItemHelper.authorizedDelete(g.getItem(), user,ci,true);
            } catch (Throwable e) {
                logger.error("",e);
            }

	        try {
				EventManager.Trigger(Groups.getGroupDatatype(),g.getId(),Event.DELETE);
			} catch (Exception e1) {
	            logger.error("",e1);
			}
			
			try {
				PoolDBUtils.ClearCache(null, user.getUsername(), Groups.getGroupDatatype());
			} catch (Exception e) {
	            logger.error("",e);
			}
        }

	}
}
