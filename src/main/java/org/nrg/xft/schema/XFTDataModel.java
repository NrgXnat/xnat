/*
 * org.nrg.xft.schema.XFTDataModel
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xft.schema;

import java.io.File;
import java.util.ArrayList;

import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.utils.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XFTDataModel {
    private static final Logger _log = LoggerFactory.getLogger(XFTDataModel.class);

    public String    db           = "";
    public String    fileLocation = "";
    public String    fileName     = "";
    public String    packageName  = "";
    public XFTSchema schema       = null;

    /**
     * ID of the database which this schema uses.
     *
     * @return The database ID.
     */
    public String getDb() {
        return db;
    }

    /**
     * Returns the local xml names of each element specified in this schema.
     *
     * @return ArrayList of Strings
     */
    @SuppressWarnings("unused")
    public ArrayList getElementNames() {
        return schema.getSortedElementNames();
    }

    /**
     * Location of the schema file.
     *
     * @return The file location.
     * @see XFTDataModel#getFullFileSpecification()
     */
    public String getFileLocation() {
        return fileLocation;
    }

    /**
     * name of the schema file (will be used to uniquely identify the schema).
     *
     * @return The file name.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the file's {@link #setFileLocation(String) location} and {@link #setFileName(String) name}.
     *
     * @return The full file specification.
     */
    public String getFullFileSpecification() {
        if (!fileLocation.endsWith(File.separator)) {
            fileLocation += File.separator;
        }
        return fileLocation + fileName;
    }

    public String getFolderName() {
        String temp = fileLocation;

        final String fs = !temp.contains("/") ? File.separator : "/";

        if (temp.endsWith(File.separator)) {
            temp = temp.substring(0, temp.length() - 1);
        }

        return temp.substring(temp.lastIndexOf(fs) + 1);
    }

    /**
     * if the schema has been populated it is returned, else it is populated and returned.
     *
     * @return The schema.
     */
    public XFTSchema getSchema() {
        if (schema == null) {
            try {
                setSchema();
            } catch (XFTInitException | ElementNotFoundException e) {
                _log.error("An error occurred setting the data model schema", e);
            }
        }
        return schema;
    }

    /**
     * schema's target namespace prefix
     *
     * @return The schema abbreviation.
     */
    @SuppressWarnings("unused")
    public String getSchemaAbbr() {
        return getSchema().getTargetNamespacePrefix();
    }

    /**
     * schema's target namespace URI
     *
     * @return The URI.
     */
    @SuppressWarnings("unused")
    public String getURI() {
        return getSchema().getTargetNamespaceURI();
    }

    /**
     * @param db The database to set.
     */
    public void setDb(String db) {
        this.db = db;
    }

    /**
     * @param location The file location (folder).
     */
    public void setFileLocation(String location) {
        if (!location.endsWith(File.separator)) {
            location = location + File.separator;
        }
        fileLocation = location;
    }

    /**
     * @param name The file name.
     */
    public void setFileName(String name) {
        fileName = name;
    }

    /**
     * Sets the data model's schema by extrapolating from the {@link #setFileLocation(String)} and {@link
     * #setFileName(String)} properties.
     *
     * @throws XFTInitException         When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     */
    public void setSchema() throws XFTInitException, ElementNotFoundException {
        this.schema = new XFTSchema(XMLUtils.GetDOM(new File(this.fileLocation + this.fileName)), fileLocation, this);
    }

    /**
     * Sets the data model's schema.
     *
     * @param schema The schema to set.
     */
    public void setSchema(final XFTSchema schema) {
        this.schema = schema;
        schema.setDataModel(this);
    }

    public String toString() {
        return getSchema().toString();
    }

    /**
     * @return Returns the packageName.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * @param packageName The packageName to set.
     */
    @SuppressWarnings("unused")
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}

