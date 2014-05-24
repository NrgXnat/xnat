//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 13, 2005
 *
 */
package org.nrg.xft;

import java.io.Externalizable;
import java.io.OutputStream;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

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
public interface ItemI extends Externalizable{
	/**
	 * Can take the sql name of a local field, or the XML dot syntax name for child fields.
	 * @param name
	 * @param default_value
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	public boolean getBooleanProperty(String name,boolean default_value) throws XFTInitException,ElementNotFoundException,FieldNotFoundException;
	
	/**
	 * Can take the sql name of a local field, or the XML dot syntax name for child fields.
	 * @param name
	 * @param default_value
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	public boolean getBooleanProperty(String name,String default_value) throws XFTInitException,ElementNotFoundException,FieldNotFoundException;
	/**
	 * Can take the sql name of a local field, or the XML dot syntax name for child fields.
	 * @param name
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	public Boolean getBooleanProperty(String name) throws XFTInitException,ElementNotFoundException,FieldNotFoundException;
	/**
	 * Can take the sql name of a local field, or the XML dot syntax name for child fields.
	 * @param name
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	public Object getProperty(String name) throws XFTInitException,ElementNotFoundException,FieldNotFoundException;
	
    public Date getDateProperty(String name)throws XFTInitException,ElementNotFoundException,FieldNotFoundException,ParseException;
	public boolean hasProperty(String id, Object find) throws XFTInitException,ElementNotFoundException,FieldNotFoundException;
	/**
	 * Can take the sql name of a local field, or the XML dot syntax name for child fields.
	 * @param xmlPath
	 * @param value
	 * @throws Exception
	 */
	public void setProperty(String xmlPath,Object value) throws Exception;
	//public void setChildItem(String field_name, ItemI item) throws XFTInitException,ElementNotFoundException,FieldNotFoundException;
	/**
	 * Gets an ArrayList of the XFTItems which are specified by the supplied field reference.
	 * @param field
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	public java.util.ArrayList getChildItems(XFTFieldWrapper field)throws XFTInitException,ElementNotFoundException,FieldNotFoundException;
	public ArrayList getChildItems(XFTFieldWrapper field,boolean includeHistory)throws XFTInitException,ElementNotFoundException,FieldNotFoundException;
	/**
	 * Gets an ItemCollection of the XFTItems which are specified by the supplied field reference.
	 * @param field
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	public ItemCollection getChildItemCollection(XFTFieldWrapper field)throws XFTInitException,ElementNotFoundException,FieldNotFoundException;
	/**
	 * Gets all XFTItem child items.
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	public java.util.ArrayList getChildItems()throws XFTInitException,ElementNotFoundException,FieldNotFoundException;
	/**
	 * returns parent Item (if there is one).
	 * @return
	 */
	public ItemI getParent();
	/**
	 * sets the parent item for this item.
	 * @param item
	 */
	public void setParent(ItemI item);
	/**
	 * extends the item (loads any additional children).
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws DBPoolException
	 * @throws java.sql.SQLException
	 * @throws Exception
	 */
	public void extend(boolean allowMultiples) throws XFTInitException,ElementNotFoundException,DBPoolException,java.sql.SQLException,Exception;
	/**
	 * Translates this item to an HTML representation.
	 * @return
	 * @throws Exception
	 */
	public String toHTML() throws Exception;
	/**
	 * Translates this item to its XML representation using DOM.
	 * @return
	 * @throws Exception
	 */
	public Document toXML() throws Exception;
    
    /**
     * Translates this item to its XML representation.
     * @return
     * @throws Exception
     */
    public void toXML(OutputStream out, String schemaDir,boolean allowDBAccess) throws java.lang.IllegalArgumentException, org.xml.sax.SAXException;
    
    /**
     * Translates this item to its XML representation.
     * @return
     * @throws Exception
     */
    public void toXML(Writer out, String schemaDir,boolean allowDBAccess) throws java.lang.IllegalArgumentException, org.xml.sax.SAXException;
    
    /**
     * Translates this item to its XML representation.
     * @return
     * @throws Exception
     */
    public void toXML(OutputStream out,boolean allowDBAccess) throws java.lang.IllegalArgumentException, org.xml.sax.SAXException;
    
    /**
     * Translates this item to its XML representation.
     * @return
     * @throws Exception
     */
    public void toXML(Writer out,boolean allowDBAccess) throws java.lang.IllegalArgumentException, org.xml.sax.SAXException;
    
	/**
	 * Gets the data type name of this item.
	 * @return
	 */
	public String getXSIType() ;
	/**
	 * returns if this item is activated.
	 * @return
	 */
	public boolean isActive()throws MetaDataException;
	/**
	 * returns Hashtable of this item's properties.
	 * @return
	 */
	public Hashtable getProps() ;
	/**
	 * Stores this item's data in the database.
	 * @param user
	 * @throws Exception
	 */
	public boolean save(UserI user, boolean overrideSecurity, boolean allowItemRemoval,EventMetaI c) throws Exception;
	public void save(UserI user, boolean overrideSecurity,boolean quarantine,boolean overrideQuarantine, boolean allowItemRemoval,EventMetaI c) throws Exception;
	/**
	 * Save item data to DB.
	 * @param location
	 * @return
	 * @throws Exception
	 */
	public java.io.ByteArrayOutputStream toXML_BOS(String location) throws Exception;
	/**
	 * Activates this item and all of its children.
	 * @param user
	 * @throws Exception
	 */
	public void activate(UserI user) throws Exception;
	/**
	 * Lock this item and all of its children.
	 * @param user
	 * @throws Exception
	 */
	public void lock(UserI user) throws Exception;
	/**
	 * Quarantine this item and all of its children.
	 * @param user
	 * @throws Exception
	 */
	public void quarantine(UserI user) throws Exception;
	/**
	 * Validates this item's content.
	 * @return
	 * @throws Exception
	 */
	public ValidationResults validate() throws Exception;
	/**
	 * Can take the sql name of a local field, or the XML dot syntax name for child fields.
	 * @param name
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	public String getStringProperty(String name) throws XFTInitException,ElementNotFoundException,FieldNotFoundException;
	/**
	 * Returns a new XFTItem which contains the current version of this item's data from the database.
	 * @return
	 */
	public XFTItem getCurrentDBVersion();
	/**
	 * Returns a new XFTItem which contains the current version of this item's data from the database.
	 * @return
	 */
	public XFTItem getCurrentDBVersion(boolean withChildren);
	/**
	 * Whether or not this user needs Activation
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public boolean needsActivation() throws Exception;
	/**
	 * Whether or not this user can read this Item.
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public boolean canRead(UserI user) throws Exception;
	public boolean canCreate(UserI user) throws Exception;
	public boolean canDelete(UserI user) throws Exception;
	/**
	 * Whether or not this user can edit this Item.
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public boolean canEdit(UserI user) throws Exception;
	/**
	 * Whether or not this user can activate this Item.
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public boolean canActivate(UserI user) throws Exception;
	/**
	 * Returns the possible value for this XML field (based on enumerations in the XML Schema).
	 * @param xmlPath
	 * @return
	 * @throws Exception
	 */
	public ArrayList getPossibleValues(String xmlPath) throws Exception;
	/**
	 * Returns a collection of this XFTItems which are identified by the XML reference field (dot-syntax).
	 * @param xmlPath
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	public ArrayList<XFTItem> getChildItems(String xmlPath)throws XFTInitException,ElementNotFoundException,FieldNotFoundException;

	public UserI getUser();
	
	public XFTItem getItem();
	
	public String getDBName();
	
	public Document toJoinedXML() throws Exception;
	
	/**
	 * @return outputs Data based on a velocity template
	 */
	public String output();
	
	/**
	 * @param velocityTemplateName
	 * @return
	 */
	public String output(String velocityTemplateName);
}

