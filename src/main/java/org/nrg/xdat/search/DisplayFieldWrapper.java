/*
 * core: org.nrg.xdat.search.DisplayFieldWrapper
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.search;

import org.nrg.xdat.collections.DisplayFieldCollection.DisplayFieldNotFoundException;
import org.nrg.xdat.display.*;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.identifier.Identifier;
import org.nrg.xft.sequence.SequentialObject;
import java.util.*;

/**
 * @author Tim
 */
public class DisplayFieldWrapper implements Identifier, SequentialObject, DisplayFieldReferenceI {
    private DisplayField df = null;
    private String id = null;
    private int sequence = 0;
    private String type = null;
    private String header = null;
    private Boolean visible = null;

    private List<String> secondaryFields = new ArrayList<>();
    private List<String> xmlPathNames = new ArrayList<>();
    private List<String> viewCols = new ArrayList<>();

    /**
     * Creates a new display field wrapper from the submitted {@link DisplayField display field instance.}
     *
     * @param displayField The display field to wrap.
     */
    public DisplayFieldWrapper(final DisplayField displayField) {
        df = displayField;
        id = df.getParentDisplay().getElementName() + "." + df.getId();

        for (final DisplayFieldElement dfe : df.getElements()) {
            if ((!xmlPathNames.contains(dfe.getSchemaElementName())) && (!dfe.getSchemaElementName().equals("")))
                xmlPathNames.add(dfe.getSchemaElementName());
            else if (!dfe.getViewName().equals("")) {
                viewCols.add(dfe.getViewName() + "." + dfe.getViewColumn());
            }
        }

        if (df.getHtmlLink() != null) {
            HTMLLink link = df.getHtmlLink();
            for (final HTMLLinkProperty prop : link.getProperties()) {
                for (final String valueField : prop.getInsertedValues().values()) {
                    if (!valueField.equals("@WHERE")) {
                        DisplayField df2 = df.getParentDisplay().getDisplayField(valueField);
                        if (df2 == null) {
                            System.out.println("UNABLE TO FIND DISPLAY FIELD:" + df.getParentDisplay() + "." + valueField);
                        } else {
                            secondaryFields.add(df.getParentDisplay().getElementName() + "." + valueField);
                            for (final DisplayFieldElement dfe : df2.getElements()) {
                                if ((!xmlPathNames.contains(dfe.getSchemaElementName())) && (!dfe.getSchemaElementName().equals("")))
                                    xmlPathNames.add(dfe.getSchemaElementName());
                                else if (!dfe.getViewName().equals("")) {
                                    viewCols.add(dfe.getViewName() + "." + dfe.getViewColumn());
                                }
                            }
                        }
                    }
                }
            }

            Enumeration values = link.getSecureProps().keys();
            while (values.hasMoreElements()) {
                String valueField = (String) values.nextElement();
                secondaryFields.add(df.getParentDisplay().getElementName() + "." + valueField);

                DisplayField df2 = df.getParentDisplay().getDisplayField(valueField);
                for (final DisplayFieldElement dfe : df2.getElements()) {
                    if ((!xmlPathNames.contains(dfe.getSchemaElementName())) && (!dfe.getSchemaElementName().equals("")))
                        xmlPathNames.add(dfe.getSchemaElementName());
                    else if (!dfe.getViewName().equals("")) {
                        viewCols.add(dfe.getViewName() + "." + dfe.getViewColumn());
                    }
                }
            }
        }
    }

    /**
     * Gets the view columns.
     *
     * @return Returns the view columns.
     */
    @SuppressWarnings("unused")
    public List<String> getViewCols() {
        return viewCols;
    }

    /**
     * Sets the view columns for the display field.
     *
     * @param viewCols The view columns to set.
     */
    @SuppressWarnings("unused")
    public void setViewCols(final List<String> viewCols) {
        this.viewCols.clear();
        this.viewCols.addAll(viewCols);
    }

    /**
     * @return Returns the xmlPathNames.
     */
    @SuppressWarnings("unused")
    public List<String> getXmlPathNames() {
        return xmlPathNames;
    }

    /**
     * @return Returns the secondaryFields.
     */
    public List<String> getSecondaryFields() {
        return secondaryFields;
    }

    /**
     * @return Returns the df.
     */
    public DisplayField getDf() {
        return df;
    }

    /**
     * @param df The df to set.
     */
    public void setDf(DisplayField df) {
        this.df = df;
    }

    public String getId() {
        return id;
    }

    /**
     * @param id The id to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return Returns the sequence.
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * @param sequence The sequence to set.
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getLightColor() {
        return this.getDf().getLightColor();
    }

    public String getDarkColor() {
        return this.getDf().getDarkColor();
    }

    public Integer getHeaderCellWidth() {
        return null;
    }

    public Integer getHeaderCellHeight() {
        return null;
    }

    public String getHeaderCellAlign() {
        return null;
    }

    public String getHeaderCellVAlign() {
        return null;
    }

    public String getElementName() {
        return this.getDf().getParentDisplay().getElementName();
    }

    public DisplayField getDisplayField() {
        return this.getDf();
    }

    /**
     * Gets the display field header.
     *
     * @return The display field header.
     */
    public String getHeader() {
        if (header == null || header.equals(""))
            return this.getDisplayField().getHeader();
        else
            return header;
    }

    public void setHeader(String s) {
        header = s;
    }

    public String getRowID() {
        String key = this.getDisplayField().getId();
        if (this.getValue() != null){
            key = df.getId() + "_" + DisplaySearch.cleanColumnName(this.getValue().toString());
        }
        return key;
    }

    public String getElementSQLName() throws XFTInitException, ElementNotFoundException {
        return this.getDisplayField().getParentDisplay().getSchemaElement().getSQLName();
    }

    public String toString() {
        return getId();
    }

    public Integer getHTMLCellWidth() {
        return getDisplayField().getHtmlCell().getWidth();
    }

    public Integer getHTMLCellHeight() {
        return getDisplayField().getHtmlCell().getHeight();
    }

    public String getHTMLCellAlign() {
        return getDisplayField().getHtmlCell().getAlign();
    }

    public String getHTMLCellVAlign() {
        return getDisplayField().getHtmlCell().getValign();
    }

    public String getSortBy() {
        return getDisplayField().getSortBy();
    }

    public HTMLLink getHTMLLink() {
        return getDisplayField().getHtmlLink();
    }

    public boolean isImage() {
        return getDisplayField().isImage();
    }

    public boolean isHtmlContent() {
        return getDisplayField().isHtmlContent();
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
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
        if (value != null)
        	id = df.getParentDisplay().getElementName() + "." + df.getId() + "." + value;
        else
        	id = df.getParentDisplay().getElementName() + "." + df.getId();
    }

    public void setVisible(String v) {
        if (v != null) {
            visible = v.equalsIgnoreCase("true");
        	}
    	}

    public void setVisible(Boolean v) {
        visible = v;
    }

    public boolean isVisible() throws DisplayFieldNotFoundException {
        if (visible == null) {
    		return this.getDisplayField().isVisible();
        } else {
    		return visible;
    	}
    }
}
