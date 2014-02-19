/*
 * org.nrg.xdat.security.PermissionSet
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.security;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xdat.search.ElementCriteria;
import org.nrg.xft.ItemI;
import org.nrg.xft.ItemWrapper;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.MetaDataException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.SearchCriteria;
import org.nrg.xft.utils.StringUtils;
/**
 * @author Tim
 *
 */
@SuppressWarnings("serial")
public class PermissionSet{
    static org.apache.log4j.Logger logger = Logger.getLogger(PermissionSet.class);
	List<PermissionCriteria> permCriteria = null;
	List<PermissionSet> permSets = null;
	private String method=null;
	
	public PermissionSet(ItemI i) throws XFTInitException,ElementNotFoundException,FieldNotFoundException,Exception
	{
		this.setMethod(i.getStringProperty("method"));
		
		permCriteria = new ArrayList<PermissionCriteria>();
		permSets = new ArrayList<PermissionSet>();
		
		Iterator items = i.getChildItems(org.nrg.xft.XFT.PREFIX + ":field_mapping_set.allow").iterator();
		while (items.hasNext())
		{
			ItemI sub = (ItemI)items.next();
			permCriteria.add(new PermissionCriteria(sub));
		}

		items = i.getChildItems(org.nrg.xft.XFT.PREFIX + ":field_mapping_set.sub_set").iterator();
		while (items.hasNext())
		{
			ItemI sub = (ItemI)items.next();
			permSets.add(new PermissionSet(sub));
		}
	}
	
	public static final String SCHEMA_ELEMENT_NAME="xdat:field_mapping_set";
	
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
		method=StringUtils.intern(m);
	}
    
    public CriteriaCollection getXDATCriteria(SchemaElement root, String action) throws Exception
    {
        final ElementSecurity es = root.getElementSecurity();
        
        final CriteriaCollection coll = new CriteriaCollection(getMethod());
        final List<String> checkedValues = new ArrayList<String>();
        
        for (PermissionCriteria c:permCriteria)
        {            
            checkedValues.add(c.getFieldValue().toString());
            
            boolean can = false;
            
            if (!can){
                if (c.isActive())
                {
                    can=c.getAction(action);
                }
            }
            
            if (can){
                final DisplayField df = root.getDisplayFieldForXMLPath(c.getField());
                if (df == null|| !df.generatedFor.equals(""))
                {
                    final ElementCriteria ec = new ElementCriteria();
                    ec.setFieldWXMLPath(c.getField());
                    ec.setValue(c.getFieldValue());
                    coll.addClause(ec);
                }else{
                    final DisplayCriteria newC = new DisplayCriteria();
                    newC.setSearchFieldByDisplayField(root.getFullXMLName(),df.getId());
                    newC.setValue(c.getFieldValue(),false);
                    coll.addClause(newC);
                }
            }
        }        
        
        for(PermissionSet set:permSets)
        {
            final CriteriaCollection sub = set.getXDATCriteria(root,action);
            coll.addClause(sub);
        }
        return coll;
    }
	
    
    public CriteriaCollection getXFTCriteria(String action) throws Exception
    {
        final CriteriaCollection coll = new CriteriaCollection(getMethod());
        final List<String> checkedValues = new ArrayList<String>();
        
        for (PermissionCriteria c:permCriteria)
        {
            checkedValues.add(c.getFieldValue().toString());
            boolean can = false;
//            if (guestEAM!=null){
//                PermissionCriteria guestC= guestEAM.getRootPermission(c.getField(), c.getFieldValue());
//                if (guestC !=null){
//                    can =guestC.getAction(action);
//                }
//            }
            
            if (!can){
                if (c.isActive())
                {
                    can=c.getAction(action);
                }
            }
            
            if (can){
                final SearchCriteria newC = new SearchCriteria();
                newC.setFieldWXMLPath(c.getField());
                newC.setValue(c.getFieldValue());
                coll.addClause(newC);
            }
        }
 
        for(PermissionSet set:permSets)
        {
            final CriteriaCollection sub = set.getXFTCriteria(action);
            coll.addClause(sub);
        }
        return coll;
    }
	
	public boolean canAccess(String access, String headerFormat, SecurityValues row) throws ItemWrapper.FieldEmptyException,Exception
	{
		if (getMethod().equalsIgnoreCase("AND"))
		{
			boolean can = true;
            final List<String> checkedValues = new ArrayList<String>();
            
            for (PermissionCriteria criteria:permCriteria)
            {                
                checkedValues.add(criteria.getFieldValue().toString());
                
                if (criteria.isActive())
                {
                    if (!criteria.canAccess(access,headerFormat,row))
                    {
                        can = false;
                        break;
                    }
                }
			}
            
			if (can)
			{
				for (PermissionSet ps:permSets)
				{
					if (! ps.canAccess(access,headerFormat,row))
					{
						can = false;
						break;
					}
				}
			}
			return can;
		}else{
            final List<String> checkedValues = new ArrayList<String>();
            
			for (PermissionCriteria criteria: permCriteria)
			{                
                checkedValues.add(criteria.getFieldValue().toString());
                
                if (criteria.isActive())
                {
                    if (criteria.canAccess(access,headerFormat,row))
                    {
                        return true;
                    }
                }
			}
            
			for (PermissionSet ps:permSets)
			{
				if (ps.canAccess(access,headerFormat,row))
				{
					return true;
				}
			}
			return false;
		}
	}

	public boolean canReadAny()
	{
        final List<String> checkedValues = new ArrayList<String>();
        
        for (PermissionCriteria criteria:permCriteria)
        {            
            checkedValues.add(criteria.getFieldValue().toString());
            
			if (criteria.getRead() && criteria.isActive())
			{
			    return true;
			}
		}
        
		return false;
	}

	public boolean canCreateAny()
	{
		final List<String> checkedValues = new ArrayList<String>();
        
        for (PermissionCriteria criteria:permCriteria)
        {            
            checkedValues.add(criteria.getFieldValue().toString());
            
			if (criteria.getCreate() && criteria.isActive())
			{
			    return true;
			}
		}
		return false;
	}

	public boolean canEditAny()
	{
		final List<String> checkedValues = new ArrayList<String>();
        
        for (PermissionCriteria criteria:permCriteria)
        {            
            checkedValues.add(criteria.getFieldValue().toString());
            
			if (criteria.getEdit() && criteria.isActive())
			{
			    return true;
			}
		}
		return false;
	}

	public PermissionCriteria getRootPermission(String fieldName, Object value) throws Exception
	{
        for (PermissionCriteria criteria:permCriteria)
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
	        for (PermissionCriteria pc:permCriteria)
	        {
	            sb.append(pc.toString() + "\n");
	        }
	    }
	    
	    if (this.permSets.size() > 0)
	    {
	        sb.append("Sets\n");
	        for (PermissionSet ps:permSets)
	        {
	            sb.append(ps.toString() + "\n");
	        }
	    }
	    
	    return sb.toString();
	}
	
	public boolean isActive()throws MetaDataException{
        for (PermissionCriteria pc:permCriteria)
        {
            if (pc.isActive())
            {
                return true;
            }
        }
    
        for (PermissionSet ps:permSets)
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
    public List<PermissionCriteria> getPermCriteria() {
        return permCriteria;
    }
    
    
}

