/*
 * core: org.nrg.xdat.security.PermissionCriteria
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.security;

import org.nrg.xft.ItemI;
import org.nrg.xft.utils.XftStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
/**
 * @author Tim
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class PermissionCriteria implements PermissionCriteriaI{
	private static final String SPACE = " ";
	private static final String EMPTY = "";
	private static final String COMMA = ",";
	private static final String ALL = "*";
	private static final String EQUALS = "equals";
	private static final String ACTIVATE_ELEMENT = "active_element";
	private static final String CREATE_ELEMENT = "create_element";
	private static final String EDIT_ELEMENT = "edit_element";
	private static final String DELETE_ELEMENT = "delete_element";
	private static final String READ_ELEMENT = "read_element";
	private static final String COMPARISON_TYPE = "comparison_type";
	private static final String FIELD_VALUE = "field_value";
	private static final String FIELD = "field";

	private static final Logger logger = LoggerFactory.getLogger(PermissionCriteria.class);
	
	private String field=null;
	private String comparison=null;
	private Object value=null;
	private Boolean canRead=null;
	private Boolean canEdit=null;
	private Boolean canCreate=null;
	
	private Boolean canDelete=null;
	private Boolean canActivate=null;

	private boolean authorized=true;
	
	public PermissionCriteria(String elementName){
		this.elementName=elementName;
	}
	
	public PermissionCriteria(String elementName, ItemI i) throws Exception
	{
		this.elementName=elementName;
		
		setField(i.getStringProperty(FIELD));
		setFieldValue(i.getProperty(FIELD_VALUE));
		setComparisonType(i.getStringProperty(COMPARISON_TYPE));
		
		setRead(i.getBooleanProperty(READ_ELEMENT, false));
		setDelete(i.getBooleanProperty(DELETE_ELEMENT, false));
		setEdit(i.getBooleanProperty(EDIT_ELEMENT, false));
		setCreate(i.getBooleanProperty(CREATE_ELEMENT, false));
		setActivate(i.getBooleanProperty(ACTIVATE_ELEMENT, false));
		
		authorized=i.isActive();
	}
	
	public static final String SCHEMA_ELEMENT_NAME="xdat:field_mapping";
	
	/* (non-Javadoc)
	 * @see org.nrg.xdat.security.PermissionCriteriaI#getSchemaElementName()
	 */
	public String getSchemaElementName()
	{
	    return SCHEMA_ELEMENT_NAME;
	}
	
	public boolean isActive(){
		return authorized;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xdat.security.PermissionCriteriaI#getField()
	 */
	@Override
	public String getField()
	{
		return field;
	}

	public String getComparisonType()
	{
		return (comparison==null)?EQUALS:comparison;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xdat.security.PermissionCriteriaI#getFieldValue()
	 */
	@Override
	public Object getFieldValue()
	{
		return value;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xdat.security.PermissionCriteriaI#getCreate()
	 */
	@Override
	public boolean getCreate()
	{
		return (canCreate==null)?false:canCreate;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xdat.security.PermissionCriteriaI#getRead()
	 */
	@Override
	public boolean getRead()
	{
		return (canRead==null)?false:canRead;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xdat.security.PermissionCriteriaI#getEdit()
	 */
	@Override
	public boolean getEdit()
	{
		return (canEdit==null)?false:canEdit;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xdat.security.PermissionCriteriaI#getDelete()
	 */
	@Override
	public boolean getDelete()
	{
		return (canDelete==null)?false:canDelete;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xdat.security.PermissionCriteriaI#getActivate()
	 */
	@Override
	public boolean getActivate()
	{
		return (canActivate==null)?false:canActivate;
	}
	
	public boolean getAction(String action)
	{
		if(SecurityManager.CREATE.equals(action)){
			return getCreate();
		}else if(SecurityManager.READ.equals(action)){
			return getRead();
		}else if(SecurityManager.DELETE.equals(action)){
			return getDelete();
		}else if(SecurityManager.EDIT.equals(action)){
			return getEdit();
		}else if(SecurityManager.ACTIVATE.equals(action)){
			return getActivate();
		}else{
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xdat.security.PermissionCriteriaI#canAccess(java.lang.String, java.lang.String, org.nrg.xdat.security.SecurityValues)
	 */
	@Override
	public boolean canAccess(String access,SecurityValues values) throws Exception
	{
		if (getAction(access))
		{
			Object value = null;
			
			// dot syntax
			value = values.getHash().get(getField());
									
			if (value == null)
			{
				return false;
			}else
			{
				final String s = value.toString();
				final Object compareTo = getFieldValue();
				if (compareTo ==null)
				{
					return false;
				}else if (compareTo.equals(ALL)){
				    return true;
                }else{
					if (!s.contains(COMMA)){
                        if (s.equalsIgnoreCase(compareTo.toString()))
                        {
                            return true;
                        }else{
                            return false;
                        }
                    }else{
                        ArrayList<String> multiple = XftStringUtils.CommaDelimitedStringToArrayList(s, true);
                        for (String temp:multiple)
                        {
                            if (!temp.trim().equals(EMPTY) && temp.equals(compareTo.toString()))
                            {
                                return true;
                            }
                        }
                        
                        return false;
                    }
				}
			}
		}else{
			return false;
		}
	}
	
	private void setAction(String action,boolean b) throws Exception
	{
		if(SecurityManager.CREATE.equals(action)){
			this.setCreate(b);
		}else if(SecurityManager.READ.equals(action)){
			this.setRead(b);
		}else if(SecurityManager.DELETE.equals(action)){
			this.setDelete(b);
		}else if(SecurityManager.EDIT.equals(action)){
			this.setEdit(b);
		}else if(SecurityManager.ACTIVATE.equals(action)){
			this.setActivate(b);
		}
	}
	
	public void setActivate(boolean b)
	{
		canActivate=b;
	}
	
	public void setCreate(boolean b)
	{
		canCreate=b;
	}
	
	public void setRead(boolean b)
	{
		canRead=b;
	}
	
	public void setEdit(boolean b)
	{
		canEdit=b;
	}
	
	public void setDelete(boolean b)
	{
		canDelete=b;
	}
	
	public void setField(String s)
	{
		field= XftStringUtils.intern(s);
	}
		
	public void setFieldValue(Object o)
	{
		value=(o instanceof String)?((String)o).intern():o;
	}
	
	public void setComparisonType(String o)
	{
		comparison= XftStringUtils.intern(o);
	}
	
	public String toString()
	{
		final StringBuffer sb = new StringBuffer();
		sb.append(this.getField());
		sb.append(SPACE).append(getFieldValue());
		sb.append(SPACE).append(getComparisonType());
		sb.append(SPACE).append(getCreate()); 
		sb.append(SPACE).append(getRead());
		sb.append(SPACE).append(getEdit());
		sb.append(SPACE).append(getDelete());
		sb.append(SPACE).append(getActivate()); 
		
		return sb.toString();
	}
	
	final String elementName;

	@Override
	public String getElementName() {
		return elementName;
	}
}
