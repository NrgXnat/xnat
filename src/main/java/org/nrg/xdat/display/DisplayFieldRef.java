/*
 * org.nrg.xdat.display.DisplayFieldRef
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */
package org.nrg.xdat.display;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.xdat.collections.DisplayFieldCollection.DisplayFieldNotFoundException;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.sortable.Sortable;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.identifier.Identifier;
import org.nrg.xft.utils.XftStringUtils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Tim
 */
public class DisplayFieldRef extends Sortable implements Identifier, DisplayFieldReferenceI {
	static Logger logger = Logger.getLogger(DisplayFieldRef.class);
	private String id = null;
	private String header = null;
	private DisplayField df = null;
	private String elementName = null;

	private Boolean visible = null;
	private DisplayVersion parentVersion = null;
	private SchemaElement parentElement = null;
    private String type = null;

    public DisplayFieldRef(DisplayVersion dv) {
		parentVersion = dv;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.display.DisplayFieldReferenceI#getDisplayField()
	 */
    public DisplayField getDisplayField() throws DisplayFieldNotFoundException {
        if (df == null) {
			df = getParentElement().getDisplayField(id);
		}
		return df;
	}

	/**
     * Gets the reference header.
     *
     * @return The reference header.
	 */
	public String getHeader() {
        if (header == null || header.equalsIgnoreCase("")) {
            if (df != null) {
				header = df.getHeader();
            } else {
		        header = "";
		    }
		}
		return header;
	}

	/**
     * Gets the reference ID.
     *
     * @return The reference ID.
	 */
	public String getId() {
		return id;
	}

	/**
     * Sets the reference header.
     *
     * @param header The reference header.
	 */
    public void setHeader(final String header) {
        this.header = header;
	}

	/**
     * Sets the reference ID.
     *
     * @param id The reference ID.
	 */
    public void setId(final String id) {
        this.id = id;
	}

    /**
     * Gets the reference's parent {@link SchemaElement schema element}.
     *
     * @return The reference's parent {@link SchemaElement schema element}.
     */
    public SchemaElement getParentElement() {
        if (parentElement == null) {
	            try {
                if (elementName != null && (!elementName.equals(""))) {
                    parentElement = SchemaElement.GetElement(elementName);
                } else if (getParentVersion() != null) {
                    parentElement = getParentVersion().getParentElementDisplay().getSchemaElement();
                }
                    } catch (XFTInitException e) {
                logger.error("Error initializing XFT", e);
                    } catch (ElementNotFoundException e) {
                logger.error("XFT element not found: " + e.ELEMENT, e);
	        }
	    }
	    return parentElement;
	}

	/**
     * Gets the parent version.
     *
     * @return The parent version.
	 */
	public DisplayVersion getParentVersion() {
		return parentVersion;
	}

    /* (non-Javadoc)
     * @see org.nrg.xdat.display.DisplayFieldReferenceI#getLightColor()
     */
    public String getLightColor() {
        return this.getParentVersion().getLightColor();
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.display.DisplayFieldReferenceI#getDarkColor()
     */
    public String getDarkColor() {
        return this.getParentVersion().getDarkColor();
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.display.DisplayFieldReferenceI#getHeaderCellWidth()
     */
    public Integer getHeaderCellWidth() {
        return this.getParentVersion().getHeaderCell().getWidth();
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.display.DisplayFieldReferenceI#getHeaderCellHeight()
     */
    public Integer getHeaderCellHeight() {
        return this.getParentVersion().getHeaderCell().getHeight();
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.display.DisplayFieldReferenceI#getHeaderCellAlign()
     */
    public String getHeaderCellAlign() {
        return this.getParentVersion().getHeaderCell().getAlign();
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.display.DisplayFieldReferenceI#getHeaderCellVAlign()
     */
    public String getHeaderCellVAlign() {
        return this.getParentVersion().getHeaderCell().getValign();
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.display.DisplayFieldReferenceI#getElementName()
     */
    public String getElementName() {
        if (elementName == null || (elementName.equals(""))) {
            if (getParentVersion() != null) {
                elementName = this.getParentVersion().getParentElementDisplay().getElementName();
            }
        }
        return elementName;
    }

    /**
     * Sets the element name.
     *
     * @param elementName The element name to set.
     */
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.display.DisplayFieldReferenceI#getRowID()
     */
    public String getRowID() {
        try {
            String alias = this.getDisplayField().getId();
            if (this.getValue() != null)
                alias = df.getId() + "_" + StringUtils.replace(StringUtils.replace(this.getValue().toString(), ",", "_com_"), ":", "_col_");
            return alias;
        } catch (DisplayFieldNotFoundException e) {
            return id;
        }
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.display.DisplayFieldReferenceI#getElementSQLName()
     */
    public String getElementSQLName() throws XFTInitException, ElementNotFoundException {
        return this.getParentElement().getSQLName();
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.display.DisplayFieldReferenceI#getSecondaryFields()
     */
    public List<String> getSecondaryFields() {
        final List<String> secondaryFields = new ArrayList<>();
        try {
            DisplayField df = getDisplayField();
            if (df.getHtmlLink() != null) {
                HTMLLink link = df.getHtmlLink();
                for (final HTMLLinkProperty prop : link.getProperties()) {
                    for (final String valueField : prop.getInsertedValues().values()) {
                        if (!valueField.equals("@WHERE")) {
                            secondaryFields.add(df.getParentDisplay().getElementName() + "." + valueField);
                        }
                    }
                }

                Enumeration values = link.getSecureProps().keys();
                while (values.hasMoreElements()) {
                    String valueField = (String) values.nextElement();
                    secondaryFields.add(df.getParentDisplay().getElementName() + "." + valueField);
                }
            }
        } catch (DisplayFieldNotFoundException e) {
            logger.error("", e);
        }
        return secondaryFields;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.getId();
    }

    public Integer getHTMLCellWidth() {
        try {
            return getDisplayField().getHtmlCell().getWidth();
        } catch (DisplayFieldNotFoundException e) {
            return null;
        }
    }

    public Integer getHTMLCellHeight() {
        try {
            return getDisplayField().getHtmlCell().getHeight();
        } catch (DisplayFieldNotFoundException e) {
            return null;
        }
    }

    public String getHTMLCellAlign() {
        try {
            return getDisplayField().getHtmlCell().getAlign();
        } catch (DisplayFieldNotFoundException e) {
            return null;
        }
    }

    public String getHTMLCellVAlign() {
        try {
            return getDisplayField().getHtmlCell().getValign();
        } catch (DisplayFieldNotFoundException e) {
            return null;
        }
    }

    public String getSortBy() {
        try {
            return getDisplayField().getSortBy();
        } catch (DisplayFieldNotFoundException e) {
            return null;
        }
    }

    public HTMLLink getHTMLLink() {
        try {
            return getDisplayField().getHtmlLink();
        } catch (DisplayFieldNotFoundException e) {
            return null;
        }
    }

    public boolean isImage() {
        try {
            return getDisplayField().isImage();
        } catch (DisplayFieldNotFoundException e) {
            return false;
        }
    }

    public boolean isHtmlContent() {
        try {
            return getDisplayField().isHtmlContent();
        } catch (DisplayFieldNotFoundException e) {
            return false;
        }
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        if (type == null || type.equals("")) {
            return "";
        } else
            return type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }


    private Object value = null;

    public Object getValue() {
        return value;
    }

    public void setValue(Object v) {
        value = v;
    }

    public void setVisible(String v) {
        if (v != null) {
            visible = v.equalsIgnoreCase("true");
        	}
    	}

    public boolean isVisible() throws DisplayFieldNotFoundException {
        if (visible == null) {
    		return this.getDisplayField().isVisible();
        } else {
    		return visible;
    	}
    }
}

