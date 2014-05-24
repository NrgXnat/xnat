//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 13, 2005
 *
 */
package org.nrg.xdat.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.om.XdatElementAccess;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.ItemWrapper.FieldEmptyException;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.utils.StringUtils;

/**
 * @author Tim
 *
 */
public class ElementAccessManager {
	private static final Logger logger = Logger.getLogger(ElementAccessManager.class);
	private List<PermissionSet> sets = null;
	SchemaElement se = null;
	ElementDisplay ed = null;
	ElementSecurity es = null;

	private String elementName=null;
	
	public ElementAccessManager(ItemI i) throws Exception
	{
		this.setElementName(i.getStringProperty("element_name"));

		sets = new ArrayList<PermissionSet>();
		
		Iterator<XFTItem> subs = i.getChildItems(org.nrg.xft.XFT.PREFIX + ":element_access.permissions.allow_set").iterator();
		while (subs.hasNext())
		{
			ItemI sub = (ItemI)subs.next();
			PermissionSet ps = new PermissionSet(sub);

			sets.add(ps);
		}
	}

	public String toString(){
    	StringBuffer sb = new StringBuffer();
    	sb.append(this.getElement()).append("\n");
    	
    	for(PermissionSet eam:this.getPermissionSets()){
    		sb.append(eam.toString()).append("\n");
    	}
    	
    	return sb.toString();
	}

	/**
	 * @return
	 */
	public String getElement() {
		return elementName;

	}

	public void setElementName(String e)
	{
		elementName=StringUtils.intern(e);
	}

	/**
	 * @return ArrayList of PermissionSets
	 */
	public List<PermissionSet> getPermissionSets() {
		return sets;
	}

    public List<PermissionCriteria> getRootPermissions() throws Exception
    {
    	final List<PermissionSet> sets = getPermissionSets();
        if (sets.size()>0)
        {
            final PermissionSet ps = (PermissionSet)sets.get(0);
            if (ps != null)
            {
                return ps.getPermCriteria();
            }else
            {
                return new ArrayList<PermissionCriteria>();
            }
        }else{
            return new ArrayList<PermissionCriteria>();
        }
    }

	public PermissionCriteria getRootPermission(String fieldName, Object value) throws Exception
	{
		final List<PermissionSet> sets = getPermissionSets();
	    if (sets.size()>0)
	    {
			PermissionSet ps = (PermissionSet)sets.get(0);
			if (ps != null)
			{
				return ps.getRootPermission(fieldName,value);
			}else
			{
				return null;
			}
	    }else{
	        return null;
	    }
	}
	
	public static final String SCHEMA_ELEMENT_NAME="xdat:element_access";

	public String getSchemaElementName()
	{
	    return SCHEMA_ELEMENT_NAME;
	}

	public SchemaElement getSchemaElement() throws ElementNotFoundException
	{
	    if (se ==null)
	    {
		    try {
	           se= SchemaElement.GetElement(this.getElement());
	        } catch (XFTInitException e) {
	            logger.error("",e);
	        }
		}
		return se;
	}

	public ElementDisplay getElementDisplay() throws ElementNotFoundException
	{
	    if (ed==null)
	    {
	        ed=getSchemaElement().getDisplay();
	    }
	    return ed;
	}

	public ElementSecurity getElementSecurity() throws ElementNotFoundException
	{
	    if (es==null)
	    {
	        es=getSchemaElement().getElementSecurity();
	    }
	    return es;
	}



	public boolean canCreateAny()
	{
	    try {
            final ElementSecurity es = getElementSecurity();
            if (es!=null)
            {
                if (es.getPrimarySecurityFields().size()==0)
                {
                    return true;
                }
            }
        } catch (ElementNotFoundException e1) {
            logger.error("",e1);
        }

	    try {
            for (PermissionSet ps:getPermissionSets())
            {
                if (ps.canCreateAny())
                {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("",e);
        }

	    return false;
	}

	public boolean canEditAny()
	{
	    try {
            final ElementSecurity es = getElementSecurity();
            if (es!=null)
            {
                if (es.getPrimarySecurityFields().size()==0)
                {
                    return true;
                }
            }
        } catch (ElementNotFoundException e1) {
            logger.error("",e1);
        }

	    try {
            for (PermissionSet ps:getPermissionSets())
            {
                if (ps.canEditAny())
                {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("",e);
        }

	    return false;
	}

	public boolean canReadAny()
	{
	    try {
            final ElementSecurity es = getElementSecurity();
            if (es!=null)
            {
                if (es.getPrimarySecurityFields().size()==0)
                {
                    return true;
                }
            }
        } catch (ElementNotFoundException e1) {
            logger.error("",e1);
        }

	    try {
            for (PermissionSet ps:getPermissionSets())
            {
                if (ps.canReadAny())
                {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("",e);
        }

	    return false;
	}

	private final Map<String,CriteriaCollection> xftCriteria=new HashMap<String,CriteriaCollection>();

	public CriteriaCollection getXFTCriteria(final String action){
	    synchronized (xftCriteria) {
	        if (!xftCriteria.containsKey(action))
	        {
	            try {
	                final CriteriaCollection coll = new CriteriaCollection("OR");
	                final List<PermissionSet> al = getPermissionSets();
	                if (al.isEmpty()) {
	                    return null;
	                } else {
	                    for (final PermissionSet ps:al)
	                    {
	                        final CriteriaCollection sub = ps.getXFTCriteria(action);
	                        coll.addClause(sub);
	                    }

	                    xftCriteria.put(action,coll);
	                }
	            } catch (XFTInitException e) {
	                logger.error("",e);
	            } catch (FieldNotFoundException e) {
	                logger.error("",e);
	            } catch (ElementNotFoundException e) {
	                logger.error("",e);
	            } catch (Exception e) {
	                logger.error("",e);
	            }
	        }

	        return xftCriteria.get(action);
	    }
	}

    private final Map<String,CriteriaCollection> xdatCriteria=new HashMap<String,CriteriaCollection>();
    
    public CriteriaCollection getXDATCriteria(final String action){
        synchronized (xdatCriteria) {
        if (!xdatCriteria.containsKey(action))
        {
            try {
                final CriteriaCollection coll = new CriteriaCollection("OR");
                final List<PermissionSet> al = getPermissionSets();
                if (al.isEmpty()) {
                    return null;
                } else {
                	for (final PermissionSet ps:al)
                    {
                        final CriteriaCollection sub = ps.getXDATCriteria(this.getSchemaElement(),action);
                        coll.addClause(sub);
                    }

                    xdatCriteria.put(action,coll);
                }
            } catch (FieldEmptyException e) {
                logger.error("",e);
            } catch (XFTInitException e) {
                logger.error("",e);
            } catch (FieldNotFoundException e) {
                logger.error("",e);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            } catch (Exception e) {
                logger.error("",e);
            }
        }

        return xdatCriteria.get(action);
        }
    }

    public static Map<String,ElementAccessManager> GetGuestManagers(){
        Map<String,ElementAccessManager> guestManagers = new Hashtable<String,ElementAccessManager>();
        try {
            final ItemSearch search = new ItemSearch(null,"xdat:user");
            search.addCriteria("xdat:user.login", "guest");
            final ItemCollection items = search.exec(true);
            if (items.size()>0)
            {
                final XdatUser user = new XdatUser(items.first());
                for(XdatElementAccess item:user.getElementAccess())
                {
                    final ElementAccessManager eam = new ElementAccessManager(item.getItem());
                    guestManagers.put(eam.getElement(), eam);
                }
            }
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (Exception e) {
            logger.error("",e);
        }

        return guestManagers;
    }

}

