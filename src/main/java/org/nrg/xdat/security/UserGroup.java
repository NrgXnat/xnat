// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xdat.security;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xft.ItemI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.utils.StringUtils;

import com.google.common.collect.Lists;

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
    	return XdatUsergroup.getXdatUsergroupsById(id, null, true);
    }

    public void init(ItemI item) throws Exception
    {
    	this.tag=item.getStringProperty("tag");
    	tag=StringUtils.intern(tag);
    	
        accessManagers = new Hashtable<String,ElementAccessManager>();
        Iterator items = item.getChildItems("xdat:userGroup.element_access").iterator();

//        Hashtable<String,ElementAccessManager> guestManagers=ElementAccessManager.GetGuestManagers();

        while (items.hasNext())
        {
            ItemI sub = (ItemI)items.next();
            ElementAccessManager eam = new ElementAccessManager(sub);
//            if (guestManagers.containsKey(eam.getElement())){
//                eam.setGuestManager(guestManagers.get(eam.getElement()));
//            }
            accessManagers.put(eam.getElement(),eam);
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
        Iterator tempIter = temp.iterator();
        XdatStoredSearch xss = null;
        try {
            while (tempIter.hasNext())
            {
                XdatStoredSearch search = (XdatStoredSearch)tempIter.next();
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

	@Override
	public Integer getPK() {
		return pk;
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
    		if(org.apache.commons.lang.StringUtils.equals(crit.getField(), field)){
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
}
