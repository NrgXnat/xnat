//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 13, 2005
 *
 */
package org.nrg.xft;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.nrg.xdat.om.XdatUserI;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.MetaDataException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.design.XFTFieldWrapper;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.w3c.dom.Document;
/**
 * @author Tim
 *
 */
public abstract class ItemWrapper implements ItemI {
	static Logger logger = Logger.getLogger(ItemWrapper.class);
	private ItemI item = null;
	
	public boolean allowXMLDBAccess=false;//for use with the toXML(Writer) method.  Needed to allow preset to satisfy interface.
	
	public ItemWrapper(){}
	public ItemWrapper(ItemI i){item=i;}
	

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getProperty(java.lang.String)
	 */
	public Object getProperty(String sql_name) throws ElementNotFoundException,FieldNotFoundException
	{
		try {
            return getItem().getProperty(sql_name);
        } catch (XFTInitException e) {
            logger.error("",e);
        }
        return null;
	}
//	
//	public Object getProperty(String sql_name,boolean suppressError) throws FieldEmptyException,ElementNotFoundException,FieldNotFoundException
//	{
//	    try {
//            if (suppressError)
//            {
//                Object o =  item.getProperty(sql_name);
//                return o;
//            }else{
//                if (item.getProperty(sql_name)==null)
//            	{
//            		throw new FieldEmptyException(sql_name);
//            	}else
//            		return item.getProperty(sql_name);
//            }
//        } catch (XFTInitException e) {
//            return null;
//        }
//	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getBooleanProperty(java.lang.String)
	 */
	public Boolean getBooleanProperty(String sql_name) throws FieldNotFoundException
	{
	    try {
            return this.getItem().getBooleanProperty(sql_name);
        } catch (XFTInitException e) {
            logger.error("",e);
            return null;
        } catch (ElementNotFoundException e) {
            logger.error("",e);
            return null;
        }
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getBooleanProperty(java.lang.String, boolean)
	 */
	public boolean getBooleanProperty(String sql_name, boolean defaultValue) throws ElementNotFoundException,FieldNotFoundException
	{
	    try {
            return this.getItem().getBooleanProperty(sql_name,defaultValue);
        } catch (XFTInitException e) {
            logger.error("",e);
            return defaultValue;
        }
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getBooleanProperty(java.lang.String, java.lang.String)
	 */
	public boolean getBooleanProperty(String name,String default_value) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
	    if (default_value.toString().equalsIgnoreCase("1") || default_value.toString().equalsIgnoreCase("true"))
	    {
	        return getBooleanProperty(name,true);
	    }else{
	        return getBooleanProperty(name,false);
	    }
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getStringProperty(java.lang.String)
	 */
	public String getStringProperty(String sql_name) throws ElementNotFoundException,FieldNotFoundException
	{
	    try {
            return getItem().getStringProperty(sql_name);
        } catch (FieldEmptyException e) {
            logger.error("",e);
            return null;
        } catch (XFTInitException e) {
            logger.error("",e);
        }
        return null;
	}
//	
//	public String getStringProperty(String sql_name,boolean suppressError) throws FieldEmptyException,ElementNotFoundException,FieldNotFoundException
//	{
//	    if (suppressError)
//	    {
//	        try {
//                return item.getStringProperty(sql_name);
//            } catch (XFTInitException e) {
//	            return null;
//            } catch (ElementNotFoundException e) {
//	            return null;
//            } catch (FieldNotFoundException e) {
//	            return null;
//            }
//	        
//	    }else{
//		    try {
//	            return item.getStringProperty(sql_name);
//	        } catch (XFTInitException e) {
//	            return null;
//	        }
//	    }
//	}

//
//
//	public void setProperty(String sql_name,Object value) throws XFTInitException,ElementNotFoundException
//	{
//		item.setProperty(sql_name,value);
//	}
//
//	public void setChildItem(String field_name, ItemI item) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
//	{
//		item.setChildItem(field_name,item);
//	}

	/**
	 * Use the field's xmlName,sq
	 * @param field_name
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	public java.util.ArrayList getChildItems(String xmlPath)throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
	    return item.getChildItems(xmlPath);
	}

	/**
	 * Use the field's xmlPath name
	 * @param xmlPath
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	public ItemCollection getChildItemCollection(String xmlPath)throws XFTInitException,ElementNotFoundException,FieldNotFoundException,Exception
	{
	    return new ItemCollection(item.getChildItems(xmlPath));
	}
	
	public ItemI getParent()
	{
		return item.getParent();
	}
	
	public void setParent(ItemI item)
	{
		item.setParent(item);
	}
	
	public void setItem(ItemI i)
	{
		item = i;
	}
	public XFTItem getItem()
	{
	    try {
            if (item ==null)
                item = XFTItem.NewItem(getSchemaElementName(),null);
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }
		return item.getItem();
	}
	
	public void extend(boolean allowMultiples) throws XFTInitException,ElementNotFoundException,DBPoolException,java.sql.SQLException,Exception	{
		item.extend(allowMultiples);
	}
	
	public boolean isActive()throws MetaDataException
	{
	    return item.isActive();
	}

	
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#activate(org.nrg.xft.security.UserI)
	 */
	public void activate(UserI user) throws Exception
	{
	    this.getItem().activate(user);
	}
	
	public void quarantine(UserI user) throws Exception
	{
	    this.getItem().quarantine(user);
	}
	
	public void lock(UserI user) throws Exception
	{
	    this.getItem().lock(user);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#canActivate(org.nrg.xft.security.UserI)
	 */
	public boolean canActivate(UserI user) throws Exception
	{
	    return this.getItem().canActivate(user);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#canEdit(org.nrg.xft.security.UserI)
	 */
	public boolean canEdit(UserI user) throws Exception
	{
	    return this.getItem().canEdit(user);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#canEdit(org.nrg.xft.security.UserI)
	 */
	public boolean canCreate(UserI user) throws Exception
	{
	    return this.getItem().canCreate(user);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#canEdit(org.nrg.xft.security.UserI)
	 */
	public boolean canDelete(UserI user) throws Exception
	{
	    return this.getItem().canDelete(user);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#canRead(org.nrg.xft.security.UserI)
	 */
	public boolean canRead(UserI user) throws Exception
	{
	    return this.getItem().canRead(user);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getChildItemCollection(org.nrg.xft.schema.design.XFTFieldWrapper)
	 */
	public ItemCollection getChildItemCollection(XFTFieldWrapper field)throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
	    return this.getItem().getChildItemCollection(field);
	}
	
	
    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getChildItems()
     */
    public ArrayList getChildItems() throws XFTInitException,
            ElementNotFoundException, FieldNotFoundException {
        return this.getItem().getChildItems();
    }
    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getChildItems(org.nrg.xft.schema.design.XFTFieldWrapper)
     */
    public ArrayList getChildItems(XFTFieldWrapper field)
            throws XFTInitException, ElementNotFoundException,
            FieldNotFoundException {
        return this.getItem().getChildItems(field);
    }
    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getChildItems(org.nrg.xft.schema.design.XFTFieldWrapper)
     */
    public ArrayList getChildItems(XFTFieldWrapper field,boolean includeHistory)
            throws XFTInitException, ElementNotFoundException,
            FieldNotFoundException {
        return this.getItem().getChildItems(field,includeHistory);
    }
    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getCurrentDBVersion()
     */
    public XFTItem getCurrentDBVersion() {
        return this.getItem().getCurrentDBVersion();
    }
    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getCurrentDBVersion()
     */
    public XFTItem getCurrentDBVersion(boolean withChildren) {
        return this.getItem().getCurrentDBVersion(withChildren);
    }
    
    public boolean needsActivation() throws Exception
    {
        return this.getItem().needsActivation();
    }
    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getName()
     */
    public String getXSIType() {
        return this.getItem().getXSIType();
    }
    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getPossibleValues(java.lang.String)
     */
    public ArrayList getPossibleValues(String xmlPath) throws Exception {
        return this.getItem().getPossibleValues(xmlPath);
    }
    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getProps()
     */
    public Hashtable getProps() {
        return this.getItem().getProps();
    }
    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#save(org.nrg.xft.security.UserI)
     */
    public boolean save(UserI user, boolean overrideSecurity, boolean allowItemRemoval,EventMetaI c) throws Exception {
        return this.getItem().save(user,overrideSecurity,allowItemRemoval,c);
    }
    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#save(org.nrg.xft.security.UserI)
     */
    public void save(UserI user, boolean overrideSecurity, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval,EventMetaI c) throws Exception {
    	this.getItem().save(user,overrideSecurity,quarantine,overrideQuarantine,allowItemRemoval,c);
    }
    
    /**
     * Override this method to customize logic performed before this item is saved.
     * WARNING: Data may not exist in the database yet.  
     * WARNING: This may not be a complete copy of the data which is contained in the database.
     */
    public void preSave() throws Exception{
    	
    }
    
    /**
     * Override this method to customize logic performed after this item is saved.
     * WARNING: This may not be a complete copy of the data which is contained in the database.
     */
    public void postSave() throws Exception{
    	
    }
    
    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#setXMLProperty(java.lang.String, java.lang.Object)
     */
    public void setProperty(String xmlPath, Object value) throws Exception {
        this.getItem().setProperty(xmlPath, value);
    }
    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#toHTML()
     */
    public String toHTML() throws Exception {
        return this.getItem().toHTML();
    }
    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#toXML_BOS(java.lang.String)
     */
    public ByteArrayOutputStream toXML_BOS(String location) throws Exception {
        return this.getItem().toXML_BOS(location);
    }
    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#toXML()
     */
    public Document toXML() throws Exception {
        return this.getItem().toXML();
    }
    
    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#toXML()
     */
    public void toXML(OutputStream out,String schemaDir,boolean allowDBAccess) throws java.lang.IllegalArgumentException, org.xml.sax.SAXException {
        this.getItem().toXML(out,schemaDir,allowDBAccess);
    }
    
    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#toXML()
     */
    public void toXML(Writer out,String schemaDir,boolean allowDBAccess) throws java.lang.IllegalArgumentException, org.xml.sax.SAXException {
        this.getItem().toXML(out,schemaDir,allowDBAccess);
    }
    
    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#toXML()
     */
    public void toXML(OutputStream out,boolean allowDBAccess) throws java.lang.IllegalArgumentException, org.xml.sax.SAXException {
        this.getItem().toXML(out,allowDBAccess);
    }
    
    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#toXML()
     */
    public void toXML(Writer out,boolean allowDBAccess) throws java.lang.IllegalArgumentException, org.xml.sax.SAXException {
        this.getItem().toXML(out,allowDBAccess);
    }
    
    public void toXML(Writer out) throws java.lang.Exception{
    	this.toXML(out,allowXMLDBAccess);
    }
    
    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#validate()
     */
    public ValidationResults validate() throws Exception {
        return this.getItem().validate();
    }

	/**
	 * @param xmlPath
	 * @return
	 * @throws FieldNotFoundException
	 * @throws ElementNotFoundException
	 */
	public Integer getIntegerProperty(String xmlPath) throws FieldNotFoundException, ElementNotFoundException
	{
        Object o = null;
	    try {
            o =getProperty(xmlPath);
            return (Integer)o;
        } catch (ClassCastException e) {
            if (o!=null){
                try {
                    return Integer.parseInt(o.toString());
                } catch (NumberFormatException e1) {
                    return null;
                }
            }
            return null;
        } catch (FieldEmptyException e) {
            return null;
        }
	}
	
	/**
	 * @param xmlPath
	 * @return
	 * @throws FieldNotFoundException
	 * @throws ElementNotFoundException
	 */
	public Float getFloatProperty(String xmlPath) throws FieldNotFoundException, ElementNotFoundException
	{
        Object o = null;
        try {
            o =getProperty(xmlPath);
            return (Float)o;
        } catch (ClassCastException e) {
            if (o!=null){
                try {
                    return Float.parseFloat(o.toString());
                } catch (NumberFormatException e1) {
                    return null;
                }
            }
            return null;
        } catch (FieldEmptyException e) {
            return null;
        }
	}
	
	/**
	 * @param xmlPath
	 * @return
	 * @throws FieldNotFoundException
	 * @throws ElementNotFoundException
	 */
	public Double getDoubleProperty(String xmlPath) throws FieldNotFoundException, ElementNotFoundException
	{
        Object o = null;
        try {
            o =getProperty(xmlPath);
            return (Double)o;
        } catch (ClassCastException e) {
            if (o!=null){
                try {
                    return Double.parseDouble(o.toString());
                } catch (NumberFormatException e1) {
                    return null;
                }
            }
            return null;
        } catch (FieldEmptyException e) {
            return null;
        }
	}
	
	public UserI getUser()
	{
	    return getItem().getUser();
	}
	
	public String getDBName()
	{
	    return this.getItem().getDBName();
	}
	
	public boolean hasProperty(String id, Object find) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
	    return this.getItem().hasProperty(id,find);
	}
	
	public Document toJoinedXML() throws Exception
	{
	    return getItem().toJoinedXML();
	}

    @SuppressWarnings("serial")
	public class FieldEmptyException extends FieldNotFoundException
	{
		public FieldEmptyException(String field)
		{
			super(field);
		}
	};

	public abstract String getSchemaElementName();

	
	public String output(String s)
	{
	    return getItem().output(s);
	}
	
	public String output()
	{
	    return getItem().output();
	}

    
    public Date getDateProperty(String name)throws XFTInitException,ElementNotFoundException,FieldNotFoundException,ParseException{
        return this.getItem().getDateProperty(name);
    }
	
    /* (non-Javadoc)
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        getItem().readExternal(in);
    }
    /* (non-Javadoc)
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        getItem().writeExternal(out);
    }
    
    public XdatUserI getInsertUser(){
        return this.getItem().getInsertUser();
    }
    
    public Date getInsertDate(){
        return this.getItem().getInsertDate();
    }
    


	public String getLongCreateTime()
	{
	    if (((XFTItem)this.getItem()).getInsertDate() == null)
		{
			return "--";
		}else
		{
		    Date date = ((XFTItem)this.getItem()).getInsertDate();
			return DateFormat.getDateInstance(DateFormat.LONG).format(date);
		}
	}

	public XdatUserI getMetaCreatedBy()
	{
	    return ((XFTItem)this.getItem()).getInsertUser();
	}
}

