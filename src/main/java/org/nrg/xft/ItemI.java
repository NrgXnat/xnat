/*
 * org.nrg.xft.ItemI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */
package org.nrg.xft;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.OutputStream;
import java.io.Writer;
import java.sql.SQLException;
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
import org.xml.sax.SAXException;

/**
 * @author Tim
 */
public interface ItemI extends Externalizable {
    /**
     * Can take the sql name of a local field, or the XML dot syntax name for child fields.
     *
     * @param name          The name of the property.
     * @param default_value The default value to return if no value is set for the property.
     * @return The value set for the property or the default value if no value is set.
     * @throws XFTInitException When an error occurs in XFT.         When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException   When a specified field isn't found on the object.
     */
    boolean getBooleanProperty(String name, boolean default_value) throws XFTInitException, ElementNotFoundException, FieldNotFoundException;

    /**
     * Can take the sql name of a local field, or the XML dot syntax name for child fields.
     *
     * @param name          The name of the property.
     * @param default_value The default value to return if no value is set for the property.
     * @return The value set for the property or the default value if no value is set.
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException   When a specified field isn't found on the object.
     */
    boolean getBooleanProperty(String name, String default_value) throws XFTInitException, ElementNotFoundException, FieldNotFoundException;

    /**
     * Can take the sql name of a local field, or the XML dot syntax name for child fields.
     *
     * @param name The name of the property.
     * @return The value set for the property or null if no value is set.
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException   When a specified field isn't found on the object.
     */
    Boolean getBooleanProperty(String name) throws XFTInitException, ElementNotFoundException, FieldNotFoundException;

    /**
     * Can take the sql name of a local field, or the XML dot syntax name for child fields.
     *
     * @param name The name of the property.
     * @return The value set for the property or null if no value is set.
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException   When a specified field isn't found on the object.
     */
    Object getProperty(String name) throws XFTInitException, ElementNotFoundException, FieldNotFoundException;

    /**
     * Can take the sql name of a local field, or the XML dot syntax name for child fields.
     *
     * @param name The name of the property.
     * @return The value set for the property or null if no value is set.
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException   When a specified field isn't found on the object.
     * @throws ParseException           When the date value can't be parsed.
     */
    Date getDateProperty(String name) throws XFTInitException, ElementNotFoundException, FieldNotFoundException, ParseException;

    /**
     * Can take the sql name of a local field, or the XML dot syntax name for child fields.
     *
     * @param id   The name of the property.
     * @param find The object to test against.
     * @return True if the object has the indicated property matching the find object, false otherwise.
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException   When a specified field isn't found on the object.
     */
    boolean hasProperty(String id, Object find) throws XFTInitException, ElementNotFoundException, FieldNotFoundException;

    /**
     * Can take the sql name of a local field, or the XML dot syntax name for child fields.
     *
     * @param xmlPath The XML path or SQL name for the property to set.
     * @param value   The value to set for the property.
     * @throws Exception When an error occurs.
     */
    void setProperty(String xmlPath, Object value) throws Exception;

    /**
     * Gets an list of the child XFTItems which are specified by the supplied field reference.
     *
     * @param field The field to check.
     * @return A list of all of the child XFTItems.
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException   When a specified field isn't found on the object.
     */
    ArrayList getChildItems(XFTFieldWrapper field) throws XFTInitException, ElementNotFoundException, FieldNotFoundException;

    /**
     * Gets an list of the child {@link XFTItem items} which are specified by the supplied field reference.
     *
     * @param field          The field to check.
     * @param includeHistory Indicates whether the item history should be included.
     * @return A list of all of the child {@link XFTItem items}.
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException   When a specified field isn't found on the object.
     */
    ArrayList getChildItems(XFTFieldWrapper field, boolean includeHistory) throws XFTInitException, ElementNotFoundException, FieldNotFoundException;

    /**
     * Gets an {@link ItemCollection collection} of the {@link XFTItem items} which are specified by the supplied field reference.
     *
     * @param field The field to check.
     * @return A collection of XFTItem objects.
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException   When a specified field isn't found on the object.
     */
    ItemCollection getChildItemCollection(XFTFieldWrapper field) throws XFTInitException, ElementNotFoundException, FieldNotFoundException;

    /**
     * Gets all {@link XFTItem child items}.
     *
     * @return A list of all of the {@link XFTItem child items}.
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException   When a specified field isn't found on the object.
     */
    ArrayList getChildItems() throws XFTInitException, ElementNotFoundException, FieldNotFoundException;

    /**
     * Returns the {@link ItemI parent item} (if there is one).
     *
     * @return The {@link ItemI parent item}.
     */
    ItemI getParent();

    /**
     * Sets the parent item for this item.
     *
     * @param item The {@link ItemI parent item} to set.
     */
    void setParent(ItemI item);

    /**
     * Extends the item (loads any additional children).
     *
     * @param allowMultiples Indicates whether multiple extensions are allowed.
     * @throws Exception When an error occurs.
     */
    void extend(boolean allowMultiples) throws Exception;

    /**
     * Translates this item to an HTML representation.
     *
     * @return The HTML representation of the object.
     * @throws Exception When an error occurs. 
     */
    String toHTML() throws Exception;

    /**
     * Translates this item to its XML representation using DOM.
     *
     * @return The XML representation of the object in the form of a Document object.
     * @throws Exception When an error occurs. 
     */
    Document toXML() throws Exception;

    /**
     * Translates this item to its XML representation and writes it out to the submitted output stream.
     *
     * @param out           The output stream to which the XML representation should be written.
     * @param schemaDir     The directory in which any validating schema can be located.
     * @param allowDBAccess Indicates whether the database can be accessed.
     * @throws IllegalArgumentException When one of the arguments passed is invalid or not allowed.
     * @throws SAXException When an error occurs parsing the XML.
     */
    void toXML(OutputStream out, String schemaDir, boolean allowDBAccess) throws IllegalArgumentException, SAXException;

    /**
     * Translates this item to its XML representation and writes it out to the submitted writer.
     *
     * @param out           The writer to which the XML representation should be written.
     * @param schemaDir     The directory in which any validating schema can be located.
     * @param allowDBAccess Indicates whether the database can be accessed.
     * @throws IllegalArgumentException When one of the arguments passed is invalid or not allowed.
     * @throws SAXException When an error occurs parsing the XML.
     */
    void toXML(Writer out, String schemaDir, boolean allowDBAccess) throws IllegalArgumentException, SAXException;

    /**
     * Translates this item to its XML representation and writes it out to the submitted output stream.
     *
     * @param out           The output stream to which the XML representation should be written.
     * @param allowDBAccess Indicates whether the database can be accessed.
     * @throws IllegalArgumentException When one of the arguments passed is invalid or not allowed.
     * @throws SAXException When an error occurs parsing the XML.
     */
    void toXML(OutputStream out, boolean allowDBAccess) throws java.lang.IllegalArgumentException, org.xml.sax.SAXException;

    /**
     * Translates this item to its XML representation and writes it out to the submitted writer.
     *
     * @param out           The writer to which the XML representation should be written.
     * @param allowDBAccess Indicates whether the database can be accessed.
     * @throws IllegalArgumentException When one of the arguments passed is invalid or not allowed.
     * @throws SAXException When an error occurs parsing the XML.
     */
    void toXML(Writer out, boolean allowDBAccess) throws java.lang.IllegalArgumentException, org.xml.sax.SAXException;

    /**
     * Gets the data-type name of this item.
     *
     * @return The data-type name of the item.
     */
    String getXSIType();

    /**
     * Indicates whether this item is activated.
     *
     * @return True if the item is activated, false otherwise.
     * @throws MetaDataException When an error occurs retrieving or parsing the object metadata.
     */
    boolean isActive() throws MetaDataException;

    /**
     * Returns a table of this item's properties.
     *
     * @return A table of this item's properties.
     */
    Hashtable getProps();

    /**
     * Stores this item's data in the database.
     *
     * @param user             The user requesting the save.
     * @param overrideSecurity Indicates whether security should be overwritten.
     * @param allowItemRemoval Indicates whether existing items should be removed to allow this save operation.
     * @param c                Event meta data for the save operation.
     * @return True if the save operation completed properly, false otherwise.
     * @throws Exception When an error occurs. 
     */
    boolean save(UserI user, boolean overrideSecurity, boolean allowItemRemoval, EventMetaI c) throws Exception;

    /**
     * Stores this item's data in the database.
     *
     * @param user               The user requesting the save.
     * @param overrideSecurity   Indicates whether security should be overridden.
     * @param quarantine         Indicates whether object should be placed in quarantine upon completion of the save
     *                           operation.
     * @param overrideQuarantine Indicates whether quarantine should be overridden.
     * @param allowItemRemoval   Indicates whether existing items should be removed to allow this save operation.
     * @param c                  Event meta data for the save operation.
     * @throws Exception When an error occurs. 
     */
    void save(UserI user, boolean overrideSecurity, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval, EventMetaI c) throws Exception;

    /**
     * Save item data to DB.
     *
     * @param location The location from which data should be loaded.
     * @return The byte stream from the loaded XML document.
     * @throws Exception When an error occurs. 
     */
    ByteArrayOutputStream toXML_BOS(String location) throws Exception;

    /**
     * Activates this item and all of its children.
     *
     * @param user The user requesting the activation.
     * @throws Exception When an error occurs. 
     */
    void activate(UserI user) throws Exception;

    /**
     * Lock this item and all of its children.
     *
     * @param user The user requesting the lock.
     * @throws Exception When an error occurs. 
     */
    void lock(UserI user) throws Exception;

    /**
     * Quarantine this item and all of its children.
     *
     * @param user The user requesting the lock.
     * @throws Exception When an error occurs. 
     */
    void quarantine(UserI user) throws Exception;

    /**
     * Validates this item's content.
     *
     * @return The results of the validation.
     * @throws Exception When an error occurs. 
     */
    ValidationResults validate() throws Exception;

    /**
     * Can take the sql name of a local field, or the XML dot syntax name for child fields.
     *
     * @param name The name of the property.
     * @return The value of the requested property.
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException   When a specified field isn't found on the object.
     */
    String getStringProperty(String name) throws XFTInitException, ElementNotFoundException, FieldNotFoundException;

    /**
     * Returns a new XFTItem which contains the current version of this item's data from the database.
     *
     * @return The current version of the data.
     */
    XFTItem getCurrentDBVersion();

    /**
     * Returns a new XFTItem which contains the current version of this item's data from the database.
     *
     * @param withChildren Indicates whether the children should be checked as well.
     * @return The current version of the data.
     */
    XFTItem getCurrentDBVersion(boolean withChildren);

    /**
     * Whether or not this user needs Activation
     *
     * @return True if the user needs to be activated, false otherwise.
     * @throws Exception When an error occurs. 
     */
    boolean needsActivation() throws Exception;

    /**
     * Whether or not this user can read this Item.
     *
     * @param user The user to test.
     * @return True if the user can read this item, false otherwise.
     * @throws Exception When an error occurs. 
     */
    boolean canRead(UserI user) throws Exception;

    /**
     * Whether or not this user can create this Item.
     *
     * @param user The user to test.
     * @return True if the user can create this item, false otherwise.
     * @throws Exception When an error occurs. 
     */
    boolean canCreate(UserI user) throws Exception;

    /**
     * Whether or not this user can delete this Item.
     *
     * @param user The user to test.
     * @return True if the user can delete this item, false otherwise.
     * @throws Exception When an error occurs. 
     */
    boolean canDelete(UserI user) throws Exception;

    /**
     * Whether or not this user can edit this Item.
     *
     * @param user The user to test.
     * @return True if the user can edit this item, false otherwise.
     * @throws Exception When an error occurs. 
     */
    boolean canEdit(UserI user) throws Exception;

    /**
     * Whether or not this user can activate this Item.
     *
     * @param user The user to test.
     * @return True if the user can activate this item, false otherwise.
     * @throws Exception When an error occurs. 
     */
    boolean canActivate(UserI user) throws Exception;

    /**
     * Returns the possible value for this XML field (based on enumerations in the XML Schema).
     *
     * @param xmlPath The XML path to test.
     * @return The list of possible values for the field.
     * @throws Exception When an error occurs. 
     */
    ArrayList getPossibleValues(String xmlPath) throws Exception;

    /**
     * Returns a collection of this XFTItems which are identified by the XML reference field (dot-syntax).
     *
     * @param xmlPath The XML path to follow.
     * @return The list of {@link XFTItem child items}.
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException   When a specified field isn't found on the object.
     */
    ArrayList<XFTItem> getChildItems(String xmlPath) throws XFTInitException, ElementNotFoundException, FieldNotFoundException;

    /**
     * Gets the {@link UserI user} that owns this object.
     *
     * @return The owning {@link UserI user}.
     */
    UserI getUser();

    /**
     * Gets this object as an {@link XFTItem}.
     *
     * @return The {@link XFTItem} representing this object.
     */
    XFTItem getItem();

    /**
     * Gets the name of the database that contains this object.
     *
     * @return The name of the database that contains this object.
     * @deprecated The abstraction of the data source means that there is only a single data connection.
     */
    @Deprecated
    String getDBName();

    /**
     * Gets a fully joined XML representation of this object.
     *
     * @return The XML representation of this object as a Document object.
     * @throws Exception When an error occurs. 
     */
    Document toJoinedXML() throws Exception;

    /**
     * Renders the object based on its implicit Velocity template representation.
     *
     * @return Outputs text based on the implicit {@link #getXSIType() data type}-based Velocity template
     */
    String output();

    /**
     * Renders the object based on the indicated Velocity template.
     *
     * @param velocityTemplateName The name of the Velocity template to use for rendering.
     * @return Outputs text based on the indicated Velocity template.
     */
    String output(String velocityTemplateName);
}

