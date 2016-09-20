/*
 * core: org.nrg.xdat.security.UserGroup
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xdat.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.GroupFeature;
import org.nrg.xdat.om.XdatElementAccess;
import org.nrg.xdat.om.XdatFieldMapping;
import org.nrg.xdat.om.XdatFieldMappingSet;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.services.GroupFeatureService;
import org.nrg.xft.ItemI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class UserGroup implements UserGroupI{
    private Logger logger = Logger.getLogger(UserGroup.class);
	private String id=null;
	private Integer pk=null;
	private String tag=null;
	private String displayName=null;
	
	public UserGroup(XdatUsergroup gp) throws Exception{
		id=gp.getId();
		pk=gp.getXdatUsergroupId();
		displayName=gp.getDisplayname();
		init(gp.getItem());
	}
	
	public UserGroup(){
		
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xdat.security.UserGroupI#getId()
	 */
	@Override
	public String getId(){
		return id;
	}

	@Override
	public String getTag(){
		return tag;
	}

	@Override
	public String getDisplayname(){
		return displayName;
	}

	private Hashtable<String,ElementAccessManager> accessManagers = null;
	private List<String> features=null;
	private List<String> blocked=null;

    

    protected synchronized Hashtable<String,ElementAccessManager> getAccessManagers(){
        if (accessManagers==null){
            try {
                init(getUserGroupImpl());
            } catch (Exception e) {
                logger.error("",e);
            }
        }
        return accessManagers;
    }
    
    public XdatUsergroup getUserGroupImpl(){
    	if(xdatGroup==null){
    		return XdatUsergroup.getXdatUsergroupsById(id, null, true);
    	}else{
    		return xdatGroup;
    	}
    }
    
    private XdatUsergroup getSavedUserGroupImpl(UserI user){
    	if(xdatGroup==null){
    		xdatGroup=XdatUsergroup.getXdatUsergroupsById(id, user, true);
    		
    		if(xdatGroup==null){
    			xdatGroup= new XdatUsergroup((UserI)user);
    			xdatGroup.setId(this.getId());
    			xdatGroup.setTag(this.getTag());
    			xdatGroup.setDisplayname(this.getDisplayname());
    			
    		}
    	}
    	return xdatGroup;
    }

    public void init(ItemI item) throws Exception
    {
    	this.tag=item.getStringProperty("tag");
    	tag= XftStringUtils.intern(tag);
    	
        accessManagers = new Hashtable<String,ElementAccessManager>();

        for (ItemI sub :item.getChildItems("xdat:userGroup.element_access"))
        {
            ElementAccessManager eam = new ElementAccessManager(sub);
            accessManagers.put(eam.getElement(),eam);
        }

        features=Lists.newArrayList();
        blocked=Lists.newArrayList();
        for(GroupFeature feature:(XDAT.getContextService().getBean(GroupFeatureService.class).findFeaturesForGroup(this.getId()))){
        	if(feature.isBlocked()){
            	blocked.add(feature.getFeature());
        	}else if(feature.isOnByDefault()){
            	features.add(feature.getFeature());
        	}
        }
    }


    public String toString(){
    	StringBuffer sb = new StringBuffer();
    	sb.append(this.getId()).append("\n");
    	sb.append(this.getTag()).append("\n");
    	
    	for(ElementAccessManager eam:this.getAccessManagers().values()){
    		sb.append(eam.toString()).append("\n");
    	}
    	
    	return sb.toString();
    }

    private List<XdatStoredSearch> stored_searches = null;
    /**
     * @return
     */
    protected List<XdatStoredSearch> getStoredSearches()
    {
        if (this.stored_searches==null)
        {
            try {
                stored_searches= XdatStoredSearch.GetPreLoadedSearchesByAllowedGroup(id);
            } catch (Exception e) {
                logger.error("",e);
            }
        }
        return stored_searches;
    }


    /**
     * @param id
     * @return
     */
    protected XdatStoredSearch getStoredSearch(String id)
    {
        List<XdatStoredSearch> temp = getStoredSearches();
        XdatStoredSearch xss = null;
        try {
            for (XdatStoredSearch search:temp)
            {
                if (id.equalsIgnoreCase(search.getId()))
                {
                    xss= search;
                }
            }
        } catch (Exception e) {
            logger.error("",e);
        }
        return xss;
    }

    protected void replacePreLoadedSearch(XdatStoredSearch i){
        try {
            ItemI old = getStoredSearch(i.getStringProperty("ID"));
            if (old!=null){
                stored_searches.remove(old);
            }
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        }
        stored_searches.add(i);
    }

    /**
     * ArrayList: 0:elementName 1:ArrayList of PermissionItems
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public List<List<Object>> getPermissionItems(String login) throws Exception
    {
        final ArrayList<List<Object>> allElements = new ArrayList<List<Object>>();
        final List<ElementSecurity> elements = ElementSecurity.GetSecureElements();
        
        Collections.sort(elements,((ElementSecurity)elements.get(0)).getComparator());
        
        
        for (ElementSecurity es:elements)
        {
            final List<PermissionItem> permissionItems = (this.getTag()==null)?es.getPermissionItems(login):es.getPermissionItemsForTag(this.getTag());
            boolean isAuthenticated = true;
            boolean wasSet = false;
            for (PermissionItem pi:permissionItems)
            {
                final ElementAccessManager eam = this.getAccessManagers().get(es.getElementName());
                if (eam != null)
                {
                    final PermissionCriteriaI pc = eam.getMatchingPermissions(pi.getFullFieldName(),pi.getValue());
                    if (pc != null)
                    {
                        pi.set(pc);
                    }
                }
                if (!pi.isAuthenticated())
                {
                    isAuthenticated = false;
                }
                if (pi.wasSet())
                {
                    wasSet = true;
                }
            }
            
            final List<Object> elementManager = new ArrayList<Object>();
            elementManager.add(es.getElementName());
            elementManager.add(permissionItems);
            elementManager.add(es.getSchemaElement().getSQLName());
            elementManager.add((isAuthenticated)?Boolean.TRUE:Boolean.FALSE);
            elementManager.add((wasSet)?Boolean.TRUE:Boolean.FALSE);
            elementManager.add(es);
            
            if (permissionItems.size() > 0)
                allElements.add(elementManager);

        }
        return allElements;
    }

	@Override
	public Integer getPK() {
		return pk;
	}

    public Map<String,List<PermissionCriteriaI>> getAllPermissions(){
    	Map<String,List<PermissionCriteriaI>> perms=Maps.newHashMap();
    	
    	for(ElementAccessManager eam3:this.getAccessManagers().values()){
        	List<PermissionCriteriaI> criteria=Lists.newArrayList();

	        for (PermissionSetI ps:eam3.getPermissionSets()) {
	        	if(ps.isActive()){
	        		criteria.addAll(ps.getAllCriteria());
	        	}
	        }
	        
	        perms.put(eam3.getSchemaElementName(), criteria);
    	}
        
        return perms;
    }

    
    public List<PermissionCriteriaI> getPermissionsByDataType(String type){
    	List<PermissionCriteriaI> criteria=Lists.newArrayList();
    	
    	ElementAccessManager eam3;
		try {
			eam3 = this.getAccessManagers().get(type);
			
			if (eam3 == null) {
	            return criteria;
	        }

		} catch (Exception e) {
			logger.error(e);
			return criteria;
		}

        for (PermissionSetI ps:eam3.getPermissionSets()) {
        	if(ps.isActive()){
        		criteria.addAll(ps.getAllCriteria());
        	}
        }
        
        return criteria;
    }
    
    public List<PermissionCriteriaI> getPermissionsByDataTypeAndField(String dataType, String field){
    	List<PermissionCriteriaI> criteria=Lists.newArrayList();
    	
    	for(PermissionCriteriaI crit: getPermissionsByDataType(dataType)){
    		if(StringUtils.equals(crit.getField(), field)){
    			criteria.add(crit);
    		}
    	}
    	
    	return criteria;
    }

	@Override
	public void setId(String id) {
		this.id=id;
	}

	@Override
	public void setTag(String tag) {
		this.tag=tag;
	}

	@Override
	public void setDisplayname(String displayName) {
		this.displayName=displayName;
	}

	@Override
	public void setPK(Integer pk) {
		this.pk=pk;
	}
	
	public  XdatUsergroup xdatGroup=null;
	
    public void addPermission(String elementName,PermissionCriteriaI pc, UserI authenticatedUser) throws Exception
    {
    	XdatUsergroup xdatGroup=getSavedUserGroupImpl(authenticatedUser);
    	
    	XdatElementAccess xea = null;
		for(XdatElementAccess temp:xdatGroup.getElementAccess()){
			if(temp.getElementName().equals(elementName)){
				xea=temp;
				break;
			}
		}
		
		if(xea==null){
			xea=new XdatElementAccess(authenticatedUser);
			xea.setElementName(elementName);
			xdatGroup.setElementAccess(xea);
		}
		
		XdatFieldMappingSet xfms=null;
		final List<XdatFieldMappingSet> set=xea.getPermissions_allowSet();
		if(set.size()==0){
			xfms = new XdatFieldMappingSet(authenticatedUser);
			xfms.setMethod("OR");
			xea.setPermissions_allowSet(xfms);
		}else{
			xfms=set.get(0);
		}
		
		
		XdatFieldMapping xfm=null;
		
		for(XdatFieldMapping t:xfms.getAllow()){
			if(t.getField().equals(pc.getField()) && t.getFieldValue().equals(pc.getFieldValue())){
				xfm=t;
				break;
			}
		}
		
		if(xfm==null){
			xfm=new XdatFieldMapping(authenticatedUser);
			xfm.setField(pc.getField());
			xfm.setFieldValue((String)pc.getFieldValue());
			xfms.setAllow(xfm);
		}
		
		xfm.setCreateElement(pc.getCreate());
		xfm.setReadElement(pc.getRead());
		xfm.setEditElement(pc.getEdit());
		xfm.setDeleteElement(pc.getDelete());
		xfm.setActiveElement(pc.getActivate());
		xfm.setComparisonType("equals");
    }
    
    public List<String> getFeatures(){
    	return features;
    }
    
    public List<String> getBlockedFeatures(){
    	return blocked;
    }
}
