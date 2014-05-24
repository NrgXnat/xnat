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

import org.apache.log4j.Logger;
import org.nrg.xft.ItemI;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.utils.StringUtils;
/**
 * @author Tim
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class PermissionCriteria{
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

	static org.apache.log4j.Logger logger = Logger.getLogger(PermissionCriteria.class);
	
	private String field=null;
	private String comparison=null;
	private Object value=null;
	private Boolean canRead=null;
	private Boolean canEdit=null;
	private Boolean canCreate=null;
	
	private Boolean canDelete=null;
	private Boolean canActivate=null;

	private boolean authorized=true;
	
	public PermissionCriteria(){
		
	}
	
	public PermissionCriteria(ItemI i) throws Exception
	{
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
	
	public String getSchemaElementName()
	{
	    return SCHEMA_ELEMENT_NAME;
	}
	
	public boolean isActive(){
		return authorized;
	}
	
	public String getField()
	{
		return field;
	}

	public String getComparisonType()
	{
		return (comparison==null)?EQUALS:comparison;
	}
	
	public Object getFieldValue()
	{
		return value;
	}
	
	public boolean getCreate()
	{
		return (canCreate==null)?false:canCreate;
	}
	
	public boolean getRead()
	{
		return (canRead==null)?false:canRead;
	}
	
	public boolean getEdit()
	{
		return (canEdit==null)?false:canEdit;
	}
	
	public boolean getDelete()
	{
		return (canDelete==null)?false:canDelete;
	}
	
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
	
	public boolean canAccess(String access,String headerFormat,SecurityValues values) throws Exception
	{
		if (getAction(access))
		{
			Object value = null;
			
			if (headerFormat.equalsIgnoreCase(SecurityManager.SELECT_GRAND))
			{
				final Object[] field = GenericWrapperElement.GetViewBasedGrandColumnNameForXMLPath(getField());
				if (field != null)
				{
					value = values.getHash().get((String)field[0]);
				}
			}else{
				// dot syntax
				value = values.getHash().get(getField());
			}
									
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
                        ArrayList<String> multiple = StringUtils.CommaDelimitedStringToArrayList(s,true);
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
		field=StringUtils.intern(s);
	}
		
	public void setFieldValue(Object o)
	{
		value=(o instanceof String)?((String)o).intern():o;
	}
	
	public void setComparisonType(String o)
	{
		comparison=StringUtils.intern(o);
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
}
