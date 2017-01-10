/*
 * core: org.nrg.xdat.security.ElementAccessManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.ItemI;
import org.nrg.xft.ItemWrapper.FieldEmptyException;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.utils.XftStringUtils;

import com.google.common.collect.Lists;

/**
 * @author Tim
 *
 */
public class ElementAccessManager {
	private static final Logger logger = Logger.getLogger(ElementAccessManager.class);
	private List<PermissionSetI> sets = null;
	SchemaElement se = null;
	ElementDisplay ed = null;
	ElementSecurity es = null;

	private String elementName=null;
	
	public ElementAccessManager(ItemI i) throws Exception
	{
		this.setElementName(i.getStringProperty("element_name"));

		sets = new ArrayList<PermissionSetI>();
		
		Iterator<XFTItem> subs = i.getChildItems(org.nrg.xft.XFT.PREFIX + ":element_access.permissions.allow_set").iterator();
		while (subs.hasNext())
		{
			ItemI sub = (ItemI)subs.next();
			PermissionSetI ps = new PermissionSet(getElement(),sub);

			sets.add(ps);
		}
	}

	public String toString(){
    	StringBuffer sb = new StringBuffer();
    	sb.append(this.getElement()).append("\n");
    	
    	for(PermissionSetI eam:this.getPermissionSets()){
    		sb.append(eam.toString()).append("\n");
    	}
    	
    	return sb.toString();
	}

	/**
	 * @return Gets the element name.
	 */
	public String getElement() {
		return elementName;

	}

	public void setElementName(String e)
	{
		elementName= XftStringUtils.intern(e);
	}

	/**
	 * @return ArrayList of PermissionSets
	 */
	public List<PermissionSetI> getPermissionSets() {
		return sets;
	}

    public List<PermissionCriteriaI> getRootPermissions() throws Exception
    {
    	final List<PermissionSetI> sets = getPermissionSets();
        if (sets.size()>0)
        {
            final PermissionSetI ps = (PermissionSet)sets.get(0);
            if (ps != null)
            {
                return ps.getPermCriteria();
            }else
            {
                return new ArrayList<PermissionCriteriaI>();
            }
        }else{
            return new ArrayList<PermissionCriteriaI>();
        }
    }

	public PermissionCriteriaI getMatchingPermissions(String fieldName, Object value) throws Exception
	{
		final List<PermissionSetI> sets = getPermissionSets();
	    if (sets.size()>0)
	    {
			PermissionSetI ps = sets.get(0);
			if (ps != null)
			{
				return ps.getMatchingPermissions(fieldName,value);
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
            for (PermissionSetI ps:getPermissionSets())
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
            for (PermissionSetI ps:getPermissionSets())
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
            for (PermissionSetI ps:getPermissionSets())
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
	                final List<PermissionSetI> al = getPermissionSets();
	                if (al.isEmpty()) {
	                    return null;
	                } else {
	                    for (final PermissionSetI ps:al)
	                    {
	                        final CriteriaCollection sub = Permissions.getXFTCriteria(ps,action);
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
                final List<PermissionSetI> al = getPermissionSets();
                if (al.isEmpty()) {
                    return null;
                } else {
                	for (final PermissionSetI ps:al)
                    {
                        final CriteriaCollection sub = Permissions.getXDATCriteria(ps,this.getSchemaElement(),action);
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

    public List<PermissionCriteriaI> getCriteria(){
    	List<PermissionCriteriaI> criteria=Lists.newArrayList();
    	for (PermissionSetI ps:this.getPermissionSets()) {
        	if(ps.isActive()){
        		criteria.addAll(ps.getAllCriteria());
        	}
        }
    	return criteria;
    }
}

