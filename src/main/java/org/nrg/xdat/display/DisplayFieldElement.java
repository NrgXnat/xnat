/*
 * org.nrg.xdat.display.DisplayFieldElement
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */
package org.nrg.xdat.display;

import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.schema.SchemaField;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.schema.design.SchemaFieldI;
import org.nrg.xft.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tim
 */
public class DisplayFieldElement {
    private static final Logger logger = LoggerFactory.getLogger(DisplayFieldElement.class);
	private String name = "";
	private String schemaElementName = "";
	private String viewName = "";
	private String viewColumn = "";
	private String xdatType = "";

    /**
     * Gets the element name.
     *
     * @return The element name.
     */
	public String getName() {
		return name;
	}

    /**
     * Gets the schema element name.
     *
     * @return The schema element name.
     */
	public String getSchemaElementName() {
		return schemaElementName;
	}

    /**
     * Gets the view column.
     *
     * @return The view column.
     */
	public String getViewColumn() {
		return viewColumn;
	}

    /**
     * Gets the view name.
     *
     * @return The view name.
     */
	public String getViewName() {
		return viewName;
	}

    /**
     * Sets the element name.
     *
     * @param name The element name.
     */
    public void setName(String name) {
        this.name = name;
	}

    /**
     * Sets the schema element name.
     *
     * @param schemaElementName The schema element name.
     */
    public void setSchemaElementName(String schemaElementName) {
        this.schemaElementName = StringUtils.StandardizeXMLPath(schemaElementName);
	}

    /**
     * Sets the view column.
     *
     * @param viewColumn The view column.
     */
    public void setViewColumn(String viewColumn) {
        this.viewColumn = viewColumn;
	}

    /**
     * Sets the view name.
     *
     * @param viewName The view name.
     */
    public void setViewName(String viewName) {
        this.viewName = viewName;
	}

    boolean checked = false;
    private Map<String, String> elementMapping = new HashMap<>();

    public String getSQLJoinedName(SchemaElementI e) {
		try {
            if (!elementMapping.containsKey(e.getFullXMLName()) && !checked) {
                checked = true;
                String sqlJoinedName = ViewManager.GetViewColumnName(e.getGenericXFTElement(), getStandardizedPath(), ViewManager.DEFAULT_LEVEL, true, true);
                if (sqlJoinedName != null) {
                    elementMapping.put(e.getFullXMLName(), sqlJoinedName);
                }
                return sqlJoinedName;
            } else if (elementMapping.containsKey(e.getFullXMLName())) {
                return elementMapping.get(e.getFullXMLName());
            } else {
                return null;
            }
        } catch (XFTInitException e1) {
            logger.error("", e1);
            return null;
        } catch (ElementNotFoundException e1) {
            logger.error("", e1);
            return null;
        }
	}

    String fieldType = null;

    public String getFieldType() {
        if (fieldType == null) {
            try {
                if (getSchemaElementName() != null && !getSchemaElementName().equals("")) {
                    GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(getSchemaElementName());
                    assert f != null;
                    fieldType = f.getXMLType().getLocalType();
                } else {
                    fieldType = "UNKNOWN";
                }
            } catch (Exception e) {
                fieldType = "UNKNOWN";
            }
        }

        return fieldType;
	}

    private String standardizedPath = null;

    public String getStandardizedPath() {
        if (standardizedPath == null) {
            standardizedPath = this.getSchemaElementName();
            if (standardizedPath.startsWith("VIEW_")) {
                standardizedPath = standardizedPath.substring(5);
            } else {
                try {
                    SchemaFieldI f = SchemaElement.GetSchemaField(standardizedPath);
                    if (f.isReference()) {
                        SchemaElementI foreign = f.getReferenceElement();
                        SchemaFieldI sf = (SchemaFieldI) foreign.getAllPrimaryKeys().get(0);
                        standardizedPath = standardizedPath + sf.getXMLPathString("");
                    }
                } catch (FieldNotFoundException e) {
                    logger.error("Field not found: " + e.FIELD, e);
                } catch (ElementNotFoundException e) {
                    logger.error("Element not found: " + e.ELEMENT, e);
                } catch (Exception e) {
                    logger.error("Unknown exception occurred", e);
                }
            }
        }

        return standardizedPath;
    }

    SchemaField sf = null;

    public SchemaField getSchemaField() throws Exception {
        if (sf == null) {
            GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(getSchemaElementName());
            sf = new SchemaField(f);
        }

        return sf;
	}

    /**
     * @return Returns the xdatType.
     */
    public String getXdatType() {
        return xdatType;
    }

    /**
     * @param xdatType The xdatType to set.
     */
    public void setXdatType(String xdatType) {
        this.xdatType = xdatType;
    }
}

