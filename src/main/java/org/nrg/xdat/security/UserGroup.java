// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xdat.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.nrg.xdat.om.XdatElementAccess;
import org.nrg.xdat.om.XdatFieldMapping;
import org.nrg.xdat.om.XdatFieldMappingSet;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xft.ItemI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.StringUtils;

public class UserGroup{
    private Logger logger = Logger.getLogger(UserGroup.class);
	private String id=null;
	private String tag=null;
	public UserGroup(String _id){
		id=_id;
	}
	
	public String getId(){
		return id;
	}
	
	public String getTag(){
		return tag;
	}

	private Hashtable<String,ElementAccessManager> accessManagers = null;

    public synchronized Hashtable<String,ElementAccessManager> getAccessManagers(){
        if (accessManagers==null){
            try {
            	XdatUsergroup temp =(XdatUsergroup) XdatUsergroup.getXdatUsergroupsById(id, null, true);
                init(temp);
            } catch (Exception e) {
                logger.error("",e);
            }
        }
        return accessManagers;
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


    public boolean getRootPermission(String elementName, String fieldName, Object value, String action) throws Exception
    {
        final PermissionCriteria pc = getRootPermissionObject(elementName,fieldName,value);
        if (pc != null)
        {
            return pc.getAction(action);
        }else{
            return false;
        }
    }

    public PermissionCriteria getRootPermissionObject(String elementName, String fieldName, Object value) throws Exception
    {
        final ElementAccessManager eam = getAccessManagers().get(elementName);
        if (eam == null)
        {
            return null;
        }else{
            return eam.getRootPermission(fieldName,value);
        }
    }

    private ArrayList<XdatStoredSearch> stored_searches = null;
    /**
     * @return
     */
    public ArrayList<XdatStoredSearch> getStoredSearches()
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
    public ItemI getStoredSearch(String id)
    {
        ArrayList<XdatStoredSearch> temp = getStoredSearches();
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

    public void replacePreLoadedSearch(XdatStoredSearch i){
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
                    final PermissionCriteria pc = eam.getRootPermission(pi.getFullFieldName(),pi.getValue());
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
    

}
