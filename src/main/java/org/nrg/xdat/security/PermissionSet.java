/*
 * core: org.nrg.xdat.security.PermissionSet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.nrg.xft.ItemI;
import org.nrg.xft.ItemWrapper;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.utils.XftStringUtils;

import com.google.common.collect.Lists;
/**
 * @author Tim
 *
 */
public class PermissionSet implements PermissionSetI{
    static org.apache.log4j.Logger logger = Logger.getLogger(PermissionSet.class);
	List<PermissionCriteriaI> permCriteria = null;
	List<PermissionSetI> permSets = null;
	private String method=null;
	
	public PermissionSet(String elementName, ItemI i) throws XFTInitException,ElementNotFoundException,FieldNotFoundException,Exception
	{
		this.setMethod(i.getStringProperty("method"));
		
		permCriteria = new ArrayList<PermissionCriteriaI>();
		permSets = new ArrayList<PermissionSetI>();
		
		for (ItemI sub:i.getChildItems(org.nrg.xft.XFT.PREFIX + ":field_mapping_set.allow"))
		{
			permCriteria.add(new PermissionCriteria(elementName,sub));
		}

		for (ItemI sub:i.getChildItems(org.nrg.xft.XFT.PREFIX + ":field_mapping_set.sub_set"))
		{
			permSets.add(new PermissionSet(elementName,sub));
		}
	}
	
	public static final String SCHEMA_ELEMENT_NAME="xdat:field_mapping_set";
	
	/* (non-Javadoc)
	 * @see org.nrg.xdat.security.PermissionSetI#getSchemaElementName()
	 */
	@Override
	public String getSchemaElementName()
	{
	    return SCHEMA_ELEMENT_NAME;
	}
	
	
	public String getMethod()
	{
		return method;
	}
	
	public void setMethod(String m)
	{
		method= XftStringUtils.intern(m);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xdat.security.PermissionSetI#canAccess(java.lang.String, java.lang.String, org.nrg.xdat.security.SecurityValues)
	 */
	@Override
	public boolean canAccess(String access, SecurityValues row) throws ItemWrapper.FieldEmptyException,Exception
	{
		if (getMethod().equalsIgnoreCase("AND"))
		{
			boolean can = true;
            final List<String> checkedValues = new ArrayList<String>();
            
            for (PermissionCriteriaI criteria:permCriteria)
            {                
                checkedValues.add(criteria.getFieldValue().toString());
                
                if (criteria.isActive())
                {
                    if (!criteria.canAccess(access,row))
                    {
                        can = false;
                        break;
                    }
                }
			}
            
			if (can)
			{
				for (PermissionSetI ps:permSets)
				{
					if (! ps.canAccess(access,row))
					{
						can = false;
						break;
					}
				}
			}
			return can;
		}else{
            final List<String> checkedValues = new ArrayList<String>();
            
			for (PermissionCriteriaI criteria: permCriteria)
			{                
                checkedValues.add(criteria.getFieldValue().toString());
                
                if (criteria.isActive())
                {
                    if (criteria.canAccess(access,row))
                    {
                        return true;
                    }
                }
			}
            
			for (PermissionSetI ps:permSets)
			{
				if (ps.canAccess(access,row))
				{
					return true;
				}
			}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.security.PermissionSetI#canReadAny()
	 */
	@Override
	public boolean canReadAny()
	{
        final List<String> checkedValues = new ArrayList<String>();
        
        for (PermissionCriteriaI criteria:permCriteria)
        {            
            checkedValues.add(criteria.getFieldValue().toString());
            
			if (criteria.getRead() && criteria.isActive())
			{
			    return true;
			}
		}
        
		return false;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.security.PermissionSetI#canCreateAny()
	 */
	@Override
	public boolean canCreateAny()
	{
		final List<String> checkedValues = new ArrayList<String>();
        
        for (PermissionCriteriaI criteria:permCriteria)
        {            
            checkedValues.add(criteria.getFieldValue().toString());
            
			if (criteria.getCreate() && criteria.isActive())
			{
			    return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.security.PermissionSetI#canEditAny()
	 */
	@Override
	public boolean canEditAny()
	{
		final List<String> checkedValues = new ArrayList<String>();
        
        for (PermissionCriteriaI criteria:permCriteria)
        {            
            checkedValues.add(criteria.getFieldValue().toString());
            
			if (criteria.getEdit() && criteria.isActive())
			{
			    return true;
			}
		}
		return false;
	}

	public PermissionCriteriaI getMatchingPermissions(String fieldName, Object value) throws Exception
	{
        for (PermissionCriteriaI criteria:permCriteria)
        {            
            if (criteria.getField().equalsIgnoreCase(fieldName) && criteria.getFieldValue().toString().equalsIgnoreCase(value.toString()))
			{
				return criteria;
			}
		}
		return null;
	}
	
//	public void addCriteria(PermissionCriteria pc) throws Exception
//	{
//		PermissionCriteria old = getRootPermission(pc.getField(),pc.getFieldValue());
//		
//		if (old != null)
//		{
//			old.setCreate(pc.getCreate());	
//			old.setRead(pc.getRead());
//			old.setEdit(pc.getEdit());
//			old.setDelete(pc.getDelete());
//			init();
//		}else{
//			getItem().setProperty(org.nrg.xft.XFT.PREFIX + ":field_mapping_set.allow",pc.getItem());
//			init();
//		}
//	}
	
	public String toString()
	{
	    final StringBuffer sb = new StringBuffer();
	    if (this.permCriteria.size() > 0)
	    {
	        sb.append("Criteria\n");
	        for (PermissionCriteriaI pc:permCriteria)
	        {
	            sb.append(pc.toString() + "\n");
	        }
	    }
	    
	    if (this.permSets.size() > 0)
	    {
	        sb.append("Sets\n");
	        for (PermissionSetI ps:permSets)
	        {
	            sb.append(ps.toString() + "\n");
	        }
	    }
	    
	    return sb.toString();
	}
	
	public boolean isActive(){
        for (PermissionCriteriaI pc:permCriteria)
        {
            if (pc.isActive())
            {
                return true;
            }
        }
    
        for (PermissionSetI ps:permSets)
        {
            if (ps.isActive())
            {
                return true;
            }
        }
	    
	    return false;
	}
    


    /**
     * @return the permCriteria
     */
    public List<PermissionCriteriaI> getPermCriteria() {
        return permCriteria;
    }


    /**
     * @return the permSet
     */
    public List<PermissionSetI> getPermSets() {
        return permSets;
    }


	@Override
	public List<PermissionCriteriaI> getAllCriteria() {
		List<PermissionCriteriaI> c=Lists.newArrayList();
		c.addAll(this.getPermCriteria());
		
		for(PermissionSetI child: getPermSets()){
			c.addAll(child.getAllCriteria());
		}
		return c;
	}
    
    
}

