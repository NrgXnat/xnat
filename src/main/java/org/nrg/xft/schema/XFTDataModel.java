/*
 * org.nrg.xft.schema.XFTDataModel
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xft.schema;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

public class XFTDataModel {
    private static final Logger _log = LoggerFactory.getLogger(XFTDataModel.class);

    public String    db           = "";
    public Resource    resource = null;
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
    public List<String> getElementNames() {
        return schema.getSortedElementNames();
    }

    /**
     * Resource for the schema file.
     *
     * @return The resource.
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * Set the Resource for the schema file.
     *
     */
    public void setResource(Resource res) {
        this.resource=res;
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
     * if the schema has been populated it is returned, else it is populated and returned.
     *
     * @return The schema.
     */
    public XFTSchema getSchema() {
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
     * @param name The file name.
     */
    public void setFileName(String name) {
        fileName = name;
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

