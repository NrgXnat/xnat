//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 25, 2005
 *
 */
package org.nrg.xdat.security;

import java.util.Comparator;

import org.nrg.xft.exception.MetaDataException;
import org.nrg.xft.utils.StringUtils;


/**
 * PermissionItem represents potential permission objects.
 * 
 * @author Tim
 *
 */
public class PermissionItem {
	private String fullFieldName = null;
	private String shortFieldName = null;
	private Object value = null;
	private String displayName = null;
	private boolean read = false;
	private boolean create = false;
	private boolean delete = false;
	private boolean edit = false;
	private boolean activate = false;
	private String comparison_type = "equals";
	private boolean authenticated = false;
	private boolean wasSet = false;
	
	public void set(PermissionCriteriaI c) throws MetaDataException
	{
		if (c.getField().equalsIgnoreCase(StringUtils.StandardizeXMLPath(fullFieldName)) && c.getFieldValue().toString().equalsIgnoreCase(value.toString()))
		{
			this.setCreate(c.getCreate());
			this.setEdit(c.getEdit());
			this.setDelete(c.getDelete());
			this.setRead(c.getRead());
			this.setActivate(c.getActivate());
			this.setComparison_type("=");
			wasSet = true;
		}
	}
	
	/**
	 * @return
	 */
	public boolean canCreate() {
		return create;
	}

	/**
	 * @return
	 */
	public boolean canDelete() {
		return delete;
	}

	/**
	 * @return
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @return
	 */
	public boolean canEdit() {
		return edit;
	}

	/**
	 * @return
	 */
	public String getFullFieldName() {
		return fullFieldName;
	}

	/**
	 * @return
	 */
	public boolean canRead() {
		return read;
	}
	/**
	 * @return
	 */
	public boolean canActivate() {
		return activate;
	}

	/**
	 * @return
	 */
	public String getShortFieldName() {
		return shortFieldName;
	}

	/**
	 * @return
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param b
	 */
	public void setCreate(boolean b) {
		create = b;
	}

	/**
	 * @param b
	 */
	public void setDelete(boolean b) {
		delete = b;
	}

	/**
	 * @param string
	 */
	public void setDisplayName(String string) {
		displayName = string;
	}

	/**
	 * @param b
	 */
	public void setEdit(boolean b) {
		edit = b;
	}

	/**
	 * @param string
	 */
	public void setFullFieldName(String string) {
		fullFieldName = string;
	}

	/**
	 * @param b
	 */
	public void setRead(boolean b) {
		read = b;
	}

	/**
	 * @param string
	 */
	public void setShortFieldName(String string) {
		shortFieldName = string;
	}

	/**
	 * @param object
	 */
	public void setValue(Object object) {
		value = object;
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(this.getFullFieldName());
		sb.append(" ").append(this.getValue());
		sb.append(" ").append(canCreate());
		sb.append(" ").append(canRead());
		sb.append(" ").append(canEdit());
		sb.append(" ").append(canDelete());
		
		return sb.toString();
	}
    /**
     * @param activate The activate to set.
     */
    public void setActivate(boolean activate) {
        this.activate = activate;
    }
    /**
     * @param comparison_type The comparison_type to set.
     */
    public void setComparison_type(String comparison_type) {
        this.comparison_type = comparison_type;
    }
    
    public String getComparison_type()
    {
        return this.comparison_type;
    }
    /**
     * @return Returns the authenticated.
     */
    public boolean isAuthenticated() {
        return authenticated;
    }
    /**
     * @param authenticated The authenticated to set.
     */
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
    /**
     * @return Returns the wasSet.
     */
    public boolean wasSet() {
        return wasSet;
    }
    
    public static Comparator GetComparator(){
        return new PermissionItem().getComparator();
    }
    
    public Comparator getComparator()
    {
        return new PIComparator();
    }

    public class PIComparator implements Comparator{
        public PIComparator()
        {
        }
        public int compare(Object o1, Object o2) {
            PermissionItem  value1 = (PermissionItem)(o1);
            PermissionItem value2 = (PermissionItem)(o2);
            
            if (value1 == null){
                if (value2 == null)
                {
                    return 0;
                }else{
                    return -1;
                }
            }
            if (value2== null)
            {
                return 1;
            }
            
            if (((Comparable)value1.getValue()).equals((Comparable)value2.getValue()))
            {
                return ((Comparable)value1.getValue()).compareTo((Comparable)value2.getValue());
            }else{
                Comparable i1 = (Comparable)value1.getValue();
                Comparable i2 = (Comparable)value2.getValue();
                int _return = i1.compareTo(i2);
                return _return;
            }
        }
    }
}

