/*
 * core: org.nrg.xft.ItemWrapper
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft;

import org.apache.log4j.Logger;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.MetaDataException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.design.XFTFieldWrapper;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.w3c.dom.Document;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

/**
 * @author Tim
 */
public abstract class ItemWrapper implements ItemI {
    static Logger logger = Logger.getLogger(ItemWrapper.class);
    private ItemI item = null;

    public boolean allowXMLDBAccess = false;//for use with the toXML(Writer) method.  Needed to allow preset to satisfy interface.

    public ItemWrapper() {
    }

    public ItemWrapper(ItemI i) {
        item = i;
    }


    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getProperty(java.lang.String)
     */
    public Object getProperty(String sql_name) throws ElementNotFoundException, FieldNotFoundException {
        try {
            return getItem().getProperty(sql_name);
        } catch (XFTInitException e) {
            logger.error("", e);
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
    public Boolean getBooleanProperty(String sql_name) throws FieldNotFoundException {
        try {
            return this.getItem().getBooleanProperty(sql_name);
        } catch (XFTInitException e) {
            logger.error("", e);
            return null;
        } catch (ElementNotFoundException e) {
            logger.error("", e);
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getBooleanProperty(java.lang.String, boolean)
     */
    public boolean getBooleanProperty(String sql_name, boolean defaultValue) throws ElementNotFoundException, FieldNotFoundException {
        try {
            return this.getItem().getBooleanProperty(sql_name, defaultValue);
        } catch (XFTInitException e) {
            logger.error("", e);
            return defaultValue;
        }
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getBooleanProperty(java.lang.String, java.lang.String)
     */
    public boolean getBooleanProperty(String name, String default_value) throws XFTInitException, ElementNotFoundException, FieldNotFoundException {
        if (default_value.equalsIgnoreCase("1") || default_value.equalsIgnoreCase("true")) {
            return getBooleanProperty(name, true);
        } else {
            return getBooleanProperty(name, false);
        }
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getStringProperty(java.lang.String)
     */
    public String getStringProperty(String sql_name) throws ElementNotFoundException, FieldNotFoundException {
        try {
            return getItem().getStringProperty(sql_name);
        } catch (FieldEmptyException e) {
            logger.error("", e);
            return null;
        } catch (XFTInitException e) {
            logger.error("", e);
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
     *
     * @param xmlPath The XML path to the property to query.
     * @return A list of the child items.
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException When a specified field isn't found on the object.
     */
    public ArrayList getChildItems(String xmlPath) throws XFTInitException, ElementNotFoundException, FieldNotFoundException {
        return item.getChildItems(xmlPath);
    }

    /**
     * Use the field's xmlPath name
     *
     * @param xmlPath The XML path to the property to query.
     * @return A {@link ItemCollection collection} of the child items.
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException When a specified field isn't found on the object.
     */
    public ItemCollection getChildItemCollection(String xmlPath) throws Exception {
        return new ItemCollection(item.getChildItems(xmlPath));
    }

    public ItemI getParent() {
        return item.getParent();
    }

    public void setParent(ItemI item) {
        item.setParent(item);
    }

    public void setItem(ItemI i) {
        item = i;
    }

    public XFTItem getItem() {
        try {
            if (item == null)
                item = XFTItem.NewItem(getSchemaElementName(), null);
        } catch (XFTInitException | ElementNotFoundException e) {
            logger.error("", e);
        }
        return item.getItem();
    }

    public void extend(boolean allowMultiples) throws Exception {
        item.extend(allowMultiples);
    }

    public boolean isActive() throws MetaDataException {
        return item.isActive();
    }


    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#activate(org.nrg.xft.security.UserI)
     */
    public void activate(UserI user) throws Exception {
        this.getItem().activate(user);
    }

    public void quarantine(UserI user) throws Exception {
        this.getItem().quarantine(user);
    }

    public void lock(UserI user) throws Exception {
        this.getItem().lock(user);
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#canActivate(org.nrg.xft.security.UserI)
     */
    public boolean canActivate(UserI user) throws Exception {
        return this.getItem().canActivate(user);
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#canEdit(org.nrg.xft.security.UserI)
     */
    public boolean canEdit(UserI user) throws Exception {
        return this.getItem().canEdit(user);
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#canEdit(org.nrg.xft.security.UserI)
     */
    public boolean canCreate(UserI user) throws Exception {
        return this.getItem().canCreate(user);
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#canEdit(org.nrg.xft.security.UserI)
     */
    public boolean canDelete(UserI user) throws Exception {
        return this.getItem().canDelete(user);
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#canRead(org.nrg.xft.security.UserI)
     */
    public boolean canRead(UserI user) throws Exception {
        return this.getItem().canRead(user);
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getChildItemCollection(org.nrg.xft.schema.design.XFTFieldWrapper)
     */
    public ItemCollection getChildItemCollection(XFTFieldWrapper field) throws XFTInitException, ElementNotFoundException, FieldNotFoundException {
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
    public ArrayList getChildItems(XFTFieldWrapper field, boolean includeHistory)
            throws XFTInitException, ElementNotFoundException,
            FieldNotFoundException {
        return this.getItem().getChildItems(field, includeHistory);
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

    public boolean needsActivation() throws Exception {
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
    public boolean save(UserI user, boolean overrideSecurity, boolean allowItemRemoval, EventMetaI c) throws Exception {
        return this.getItem().save(user, overrideSecurity, allowItemRemoval, c);
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#save(org.nrg.xft.security.UserI)
     */
    public void save(UserI user, boolean overrideSecurity, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval, EventMetaI c) throws Exception {
        this.getItem().save(user, overrideSecurity, quarantine, overrideQuarantine, allowItemRemoval, c);
    }

    /**
     * Override this method to customize logic performed before this item is saved.
     * WARNING: Data may not exist in the database yet.
     * WARNING: This may not be a complete copy of the data which is contained in the database.
     * @throws Exception When an error occurs.
     */
    public void preSave() throws Exception {

    }

    /**
     * Override this method to customize logic performed after this item is saved.
     * WARNING: This may not be a complete copy of the data which is contained in the database.
     * @throws Exception When an error occurs.
     */
    public void postSave() throws Exception {

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
    public void toXML(OutputStream out, String schemaDir, boolean allowDBAccess) throws java.lang.IllegalArgumentException, org.xml.sax.SAXException {
        this.getItem().toXML(out, schemaDir, allowDBAccess);
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#toXML()
     */
    public void toXML(Writer out, String schemaDir, boolean allowDBAccess) throws java.lang.IllegalArgumentException, org.xml.sax.SAXException {
        this.getItem().toXML(out, schemaDir, allowDBAccess);
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#toXML()
     */
    public void toXML(OutputStream out, boolean allowDBAccess) throws java.lang.IllegalArgumentException, org.xml.sax.SAXException {
        this.getItem().toXML(out, allowDBAccess);
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#toXML()
     */
    public void toXML(Writer out, boolean allowDBAccess) throws java.lang.IllegalArgumentException, org.xml.sax.SAXException {
        this.getItem().toXML(out, allowDBAccess);
    }

    public void toXML(Writer out) throws java.lang.Exception {
        this.toXML(out, allowXMLDBAccess);
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#validate()
     */
    public ValidationResults validate() throws Exception {
        return this.getItem().validate();
    }

    /**
     * Gets the value of the indicated property as an integer.
     *
     * @param xmlPath The XML path to the property to query.
     * @return The value of the indicated property.
     * @throws FieldNotFoundException When a specified field isn't found on the object.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     */
    public Integer getIntegerProperty(String xmlPath) throws FieldNotFoundException, ElementNotFoundException {
        Object o;
        try {
            o = getProperty(xmlPath);
        } catch (FieldEmptyException e) {
            return null;
        }
        try {
            return (Integer) o;
        } catch (ClassCastException e) {
            try {
                return Integer.parseInt(o.toString());
            } catch (NumberFormatException e1) {
                return null;
            }
        }
    }

    /**
     * @param xmlPath The XML path to the property to query.
     * @return The value of the indicated property as a float if found. Otherwise a {@link FieldNotFoundException} is
     * thrown.
     * @throws FieldNotFoundException When a specified field isn't found on the object.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     */
    @SuppressWarnings("unused")
    public Float getFloatProperty(String xmlPath) throws FieldNotFoundException, ElementNotFoundException {
        Object o;
        try {
            o = getProperty(xmlPath);
        } catch (FieldEmptyException e) {
            return null;
        }
        try {
            return (Float) o;
        } catch (ClassCastException e) {
            if (o != null) {
                try {
                    return Float.parseFloat(o.toString());
                } catch (NumberFormatException e1) {
                    return null;
                }
            }
            return null;
        }
    }

    /**
     * Gets the value of the indicated property as a double.
     *
     * @param xmlPath The XML path to the property to query.
     * @return The value of the indicated property.
     * @throws FieldNotFoundException When a specified field isn't found on the object.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     */
    public Double getDoubleProperty(String xmlPath) throws FieldNotFoundException, ElementNotFoundException {
        Object o;
        try {
            o = getProperty(xmlPath);
        } catch (FieldEmptyException e) {
            return null;
        }
        try {
            return (Double) o;
        } catch (ClassCastException e) {
            if (o != null) {
                try {
                    return Double.parseDouble(o.toString());
                } catch (NumberFormatException e1) {
                    return null;
                }
            }
            return null;
        }
    }

    /**
     * Gets the {@link UserI user} that owns this object.
     *
     * @return The owning {@link UserI user}.
     */
    public UserI getUser() {
        return getItem().getUser();
    }

    /**
     * Gets the name of the database that contains this object.
     *
     * @return The name of the database that contains this object.
     * @deprecated The abstraction of the data source means that there is only a single data connection.
     */
    @Deprecated
    public String getDBName() {
        return this.getItem().getDBName();
    }

    /**
     * Can take the sql name of a local field, or the XML dot syntax name for child fields.
     *
     * @param id   The name of the property.
     * @param find The object to test against.
     * @return True if the object has the indicated property matching the find object, false otherwise.
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException When a specified field isn't found on the object.
     */
    public boolean hasProperty(String id, Object find) throws XFTInitException, ElementNotFoundException, FieldNotFoundException {
        return this.getItem().hasProperty(id, find);
    }

    /**
     * Gets a fully joined XML representation of this object.
     *
     * @return The XML representation of this object as a Document object.
     * @throws Exception When an error occurs.
     */
    public Document toJoinedXML() throws Exception {
        return getItem().toJoinedXML();
    }

    @SuppressWarnings("serial")
    public class FieldEmptyException extends FieldNotFoundException {
        public FieldEmptyException(String field) {
            super(field);
        }
    }

    /**
     * Gets the schema element name.
     *
     * @return The schema element name.
     */
    public abstract String getSchemaElementName();

    /**
     * Renders the object based on the indicated Velocity template.
     *
     * @param velocityTemplateName The name of the Velocity template to use for rendering.
     * @return Outputs text based on the indicated Velocity template.
     */
    public String output(String velocityTemplateName) {
        return getItem().output(velocityTemplateName);
    }

    /**
     * Renders the object based on its implicit Velocity template representation.
     *
     * @return Outputs text based on the implicit {@link #getXSIType() data type}-based Velocity template
     */
    public String output() {
        return getItem().output();
    }

    /**
     * Can take the sql name of a local field, or the XML dot syntax name for child fields.
     *
     * @param name The name of the property.
     * @return The value set for the property or null if no value is set.
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException When a specified field isn't found on the object.
     */
    public Date getDateProperty(String name) throws XFTInitException, ElementNotFoundException, FieldNotFoundException, ParseException {
        return this.getItem().getDateProperty(name);
    }

    /**
     * The object implements the readExternal method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and readObject for objects, strings and arrays.  The
     * readExternal method must read the values in the same sequence
     * and with the same types as were written by writeExternal.
     *
     * @param in the stream to read data from in order to restore the object
     * @throws IOException            if I/O errors occur
     * @throws ClassNotFoundException If the class for an object being
     *                                restored cannot be found.
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        getItem().readExternal(in);
    }

    /**
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @param out the stream to write the object to
     * @throws IOException Includes any I/O exceptions that may occur
     * @serialData Overriding methods should use this tag to describe
     * the data layout of this Externalizable object.
     * List the sequence of element types and, if possible,
     * relate the element to a public/protected field and/or
     * method of this Externalizable class.
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        getItem().writeExternal(out);
    }

    /**
     * Gets the user that inserted this object into the database.
     *
     * @return The {@link UserI user object}.
     */
    public UserI getInsertUser() {
        return this.getItem().getInsertUser();
    }

    /**
     * Gets the date that this object was inserted into the database.
     *
     * @return The date that this object was created.
     */
    public Date getInsertDate() {
        return this.getItem().getInsertDate();
    }
}

