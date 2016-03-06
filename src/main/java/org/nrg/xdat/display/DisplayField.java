/*
 * org.nrg.xdat.display.DisplayField
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 7:53 PM
 */
package org.nrg.xdat.display;

import org.nrg.xdat.collections.DisplayFieldCollection.DisplayFieldNotFoundException;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.schema.SchemaField;
import org.nrg.xdat.search.QueryOrganizer;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.identifier.Identifier;
import org.nrg.xft.search.QueryOrganizerI;
import org.nrg.xft.sequence.SequentialObject;
import org.nrg.xft.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tim
 */
public class DisplayField implements Identifier, SequentialObject {
    private static final Logger logger = LoggerFactory.getLogger(DisplayField.class);
    private String id = "";
    private String header = "";
    private boolean image = false;
    private boolean searchable = false;
    private String dataType = null;
    private boolean visible = true;

    private String description = null;

    private final List<DisplayFieldElement> elements = new ArrayList<>();
    private final Map<String, String> content = new HashMap<>();

    private boolean htmlContent;
    private HTMLLink htmlLink = null;
    private String sortBy = "";
    private String sortOrder = "ASC";

    private HTMLCell htmlCell = new HTMLCell();
    private HTMLImage htmlImage = new HTMLImage();

    private ElementDisplay parentDisplay = null;

    private int sortIndex = 0;

    private final List<String> possibleValues = new ArrayList<>();

    public String generatedFor = "";

    public DisplayField(ElementDisplay ed) {
        parentDisplay = ed;
    }

    /**
     * Gets the header for the display field.
     *
     * @return The header for the display field.
     */
    public String getHeader() {
        return header;
    }

    /**
     * Gets the ID for the display field.
     *
     * @return The ID for the display field.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the ID for the display field. The same call as {@link #getId()}.
     *
     * @return The ID for the display field.
     * @deprecated Use {@link #getId()} instead.
     */
    @Deprecated
    public String getIdentifier() {
        return getId();
    }

    /**
     * Sets the field's header to the submitted value.
     *
     * @param header The header to set.
     */
    public void setHeader(final String header) {
        this.header = header;
    }

    /**
     * Sets the field's ID to the submitted value.
     *
     * @param id The ID to set.
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Gets a list of the elements in this display field.
     *
     * @return A list of the elements in this display field.
     */
    public List<DisplayFieldElement> getElements() {
        return elements;
    }

    /**
     * Sets the display field's elements to the submitted list of elements.
     *
     * @param elements The elements to set on the display field.
     */
    public void setElements(final List<DisplayFieldElement> elements) {
        this.elements.clear();
        this.elements.addAll(elements);
    }

    public void addDisplayFieldElement(DisplayFieldElement dfe) {
        elements.add(dfe);
    }

    /**
     * Gets a map of the content for this display field.
     *
     * @return A map of the content for this display field.
     */
    public Map<String, String> getContent() {
        return content;
    }

    /**
     * Builds and returns the SQL content of the display field based on the {@link QueryOrganizerI query organizer}.
     *
     * @param organizer The query organizer from which to build the SQL content.
     * @return The SQL content of the display field based on the query organizer.
     * @throws FieldNotFoundException When one of the requested fields can't be found in the data object.
     */
    public String getSQLContent(QueryOrganizerI organizer) throws FieldNotFoundException {
        String content = getSqlContent();

        for (final DisplayFieldElement dfe : getElements()) {
            String dfeAlias;
            if (dfe.getSchemaElementName().equalsIgnoreCase("")) {
                String viewName = getParentDisplay().getElementName() + ".";
                viewName += dfe.getViewName() + "." + dfe.getViewColumn();
                if (((QueryOrganizer) organizer).getFieldAlias(viewName) != null) {
                    dfeAlias = ((QueryOrganizer) organizer).getFieldAlias(viewName);
                } else {
                    dfeAlias = dfe.getViewName() + "_" + dfe.getViewColumn();
                }
            } else {
                if (dfe.getXdatType() == null || dfe.getXdatType().equalsIgnoreCase("")) {
                    dfeAlias = dfe.getSQLJoinedName(organizer.getRootElement());
                    if (dfeAlias == null) {
                        dfeAlias = organizer.translateStandardizedPath(dfe.getStandardizedPath());
                    }
                } else {
                    try {
                        String viewName = getParentDisplay().getElementName() + "." + dfe.getXdatType() + "_";
                        viewName += dfe.getSchemaElementName() + "." + dfe.getXdatType();
                        String temp = ((QueryOrganizer) organizer).getFieldAlias(viewName);
                        if (temp == null) {
                            dfeAlias = SchemaElement.GetElement(dfe.getSchemaElementName()).getSQLName() + "_COUNT";
                        } else {
                            dfeAlias = ((QueryOrganizer) organizer).getFieldAlias(viewName);
                        }
                    } catch (XFTInitException e) {
                        logger.error("Error initializing XFT", e);
                        dfeAlias = "'ERROR'";
                    } catch (ElementNotFoundException e) {
                        logger.error("XFT element not found: " + e.ELEMENT, e);
                        dfeAlias = "'ERROR'";
                    }
                }
            }

            if (content == null) {
                content = dfeAlias;
            } else {
                content = StringUtils.ReplaceStr(content, "@" + dfe.getName(), dfeAlias);
            }
        }
        return content;
    }

    /**
     * Gets a summary of field content.
     *
     * @return Summary of field content.
     */
    public String getSummary() {
        String content = getSqlContent();

        for (final DisplayFieldElement dfe : getElements()) {
            String dfeAlias;
            if (dfe.getSchemaElementName().equalsIgnoreCase("")) {
                dfeAlias = dfe.getViewName() + "." + dfe.getViewColumn();
            } else {
                if (dfe.getXdatType() == null || dfe.getXdatType().equalsIgnoreCase("")) {
                    dfeAlias = dfe.getSchemaElementName();
                } else {
                    try {
                        dfeAlias = SchemaElement.GetElement(dfe.getSchemaElementName()).getSQLName() + "_COUNT";
                    } catch (XFTInitException e) {
                        logger.error("Error initializing XFT", e);
                        dfeAlias = "'ERROR'";
                    } catch (ElementNotFoundException e) {
                        logger.error("XFT element not found: " + e.ELEMENT, e);
                        dfeAlias = "'ERROR'";
                    }
                }
            }

            if (content == null) {
                content = dfeAlias;
            } else {
                content = StringUtils.ReplaceStr(content, "@" + dfe.getName(), dfeAlias);
            }
        }
        return content;
    }

    /**
     * Sets the content for this display field.
     *
     * @param content The content to set for this display field.
     */
    public void setContent(final Map<String, String> content) {
        this.content.clear();
        this.content.putAll(content);
    }

    public String getSqlContent() {
        return content.get("sql");
    }

    /**
     * Indicates whether this display field represents an image.
     *
     * @return True if the display field represents an image, false otherwise.
     */
    public boolean isImage() {
        return image;
    }

    /**
     * Sets whether this field should be considered an image.
     *
     * @param image Indicates whether this field should be considered an image.
     */
    public void setImage(final boolean image) {
        this.image = image;
    }

    /**
     * Sets whether this field should be considered an image.
     *
     * @param image Indicates whether this field should be considered an image.
     */
    public void setImage(final String image) {
        this.image = Boolean.parseBoolean(image);
    }

    /**
     * Gets the associated {@link HTMLLink HTML link instance} for this display field.
     *
     * @return The associated {@link HTMLLink HTML link instance}.
     */
    public HTMLLink getHtmlLink() {
        return htmlLink;
    }

    /**
     * Sets the associated {@link HTMLLink HTML link instance} for this display field.
     *
     * @param link The {@link HTMLLink HTML link instance} to associate with this display field.
     */
    public void setHtmlLink(final HTMLLink link) {
        htmlLink = link;
    }

    /**
     * Gets the sort-by column for this display field.
     *
     * @return The sort-by column.
     */
    public String getSortBy() {
        if (sortBy.equalsIgnoreCase("")) {
            return getId();
        } else {
            return sortBy;
        }
    }

    /**
     * Gets the sort order for this display field.
     *
     * @return The sort order.
     */
    public String getSortOrder() {
        return sortOrder;
    }

    /**
     * Sets the sort-by column for this display field.
     *
     * @param sortBy The sort-by column to set.
     */
    public void setSortBy(final String sortBy) {
        this.sortBy = sortBy;
    }

    /**
     * Sets the sort order for this display field.
     *
     * @param sortOrder The sort order to set.
     */
    public void setSortOrder(final String sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * Gets the associated {@link HTMLCell HTML cell instance} for this display field.
     *
     * @return The associated {@link HTMLCell HTML cell instance}.
     */
    public HTMLCell getHtmlCell() {
        return htmlCell;
    }

    /**
     * Gets the associated {@link HTMLImage HTML image instance} for this display field.
     *
     * @return The associated {@link HTMLImage HTML image instance}.
     */
    public HTMLImage getHtmlImage() {
        return htmlImage;
    }

    /**
     * Sets the {@link HTMLCell HTML cell instance} to be associated with this display field.
     *
     * @param cell The {@link HTMLCell HTML cell instance} to associate with this display field.
     */
    @SuppressWarnings("unused")
    public void setHtmlCell(HTMLCell cell) {
        htmlCell = cell;
    }

    /**
     * Sets the {@link HTMLImage HTML image instance} to be associated with this display field.
     *
     * @param image The {@link HTMLImage HTML image instance} to associate with this display field.
     */
    @SuppressWarnings("unused")
    public void setHtmlImage(HTMLImage image) {
        htmlImage = image;
    }

    /**
     * Gets the parent element display for this display field.
     *
     * @return The parent element display for this display field.
     */
    public ElementDisplay getParentDisplay() {
        return parentDisplay;
    }

    /**
     * Gets the primary schema field associated with this display field.
     *
     * @return The primary schema field associated with ths display field.
     */
    public String getPrimarySchemaField() {
        DisplayFieldElement dfe = this.getElements().get(0);

        if (dfe.getSchemaElementName().equalsIgnoreCase("")) {
            return "VIEW_" + this.getParentDisplay().getElementName() + "." + dfe.getViewName() + "." + dfe.getViewColumn();
        } else {
            return dfe.getSchemaElementName();
        }
    }

    /**
     * @return ArrayList of Object[String path, SchemaFieldI sf]
     */
    public List<Object[]> getSchemaFields() {
        final List<Object[]> al = new ArrayList<>();
        for (final DisplayFieldElement dfe : getElements()) {
            Object[] o = new Object[2];
            if (dfe.getSchemaElementName().equalsIgnoreCase("")) {
                o[0] = "VIEW_" + this.getParentDisplay().getElementName() + "." + dfe.getViewName() + "." + dfe.getViewColumn();
                o[1] = null;
                al.add(o);
            } else {
                if (dfe.getXdatType().equalsIgnoreCase("COUNT")) {
                    o[0] = "VIEW_" + this.getParentDisplay().getElementName() + ".COUNT_" + dfe.getSchemaElementName() + ".count";
                    o[1] = null;
                    al.add(o);
                } else {
                    o[0] = dfe.getSchemaElementName();
                    try {
                        o[1] = dfe.getSchemaField();
                    } catch (XFTInitException e) {
                        logger.error("XFT failed to initialize properly.", e);
                        o[1] = null;
                    } catch (ElementNotFoundException e) {
                        logger.error("XFT element not found: " + e.ELEMENT, e);
                        o[1] = null;
                    } catch (Exception e) {
                        logger.error("Unknown error occurred", e);
                        o[1] = null;
                    }
                    al.add(o);
                }
            }
        }

        return al;
    }

    /**
     * Gets the data type associated with this display field.
     *
     * @return The data type associated with this display field.
     */
    public String getDataType() {
        if (dataType == null) {
            dataType = deriveType();
        }
        return dataType;
    }

    /**
     * Indicates whether this display field is searchable.
     *
     * @return True if the display field is searchable, false otherwise.
     */
    public boolean isSearchable() {
        return searchable;
    }

    /**
     * Sets the data type associated with this display field.
     *
     * @param dataType The data type associated with this display field.
     */
    public void setDataType(final String dataType) {
        this.dataType = dataType;
    }

    /**
     * Sets whether the display field should be searchable.
     *
     * @param searchable Whether the display field should be searchable.
     */
    public void setSearchable(final boolean searchable) {
        this.searchable = searchable;
    }

    /**
     * Sets whether the display field should be searchable.
     *
     * @param searchable Whether the display field should be searchable.
     */
    public void setSearchable(final String searchable) {
        setSearchable(Boolean.parseBoolean(searchable));
    }

    public static boolean MapTypesForQuotes(String type) {
        return type != null &&
                (type.equalsIgnoreCase("CHAR") ||
                        type.equalsIgnoreCase("VARCHAR") ||
                        type.equalsIgnoreCase("STRING") ||
                        type.equalsIgnoreCase("DATE") ||
                        type.equalsIgnoreCase("TIMESTAMP"));
    }

    /**
     * Indicates whether this display field requires quotes around values when composing SQL queries. This method calls
     * the {@link #MapTypesForQuotes(String)} method with the display field's {@link #getDataType() data type}.
     *
     * @return True if this display field's data type's values require quotes when specified in SQL, false otherwise.
     */
    public boolean needsSQLQuotes() {
        return MapTypesForQuotes(getDataType());
    }

    /**
     * Indicates whether comparisons to empty quotes work for a particular data type. You can't compare dates or
     * timestamps with empty quotes to match default values, but you can do this type of comparison for most text-based
     * data types.
     *
     * @param type The type to test.
     * @return True if you can compare values against empty quotes, false otherwise.
     */
    public static boolean MapTypesForEmptyQuotes(String type) {
        return type != null &&
                (type.equalsIgnoreCase("CHAR") ||
                        type.equalsIgnoreCase("VARCHAR") ||
                        type.equalsIgnoreCase("STRING"));
    }

    /**
     * Indicates whether this display field's value can be compared against empty quotes when composing SQL queries.
     * This method calls the {@link #MapTypesForEmptyQuotes(String)} method with the display field's {@link
     * #getDataType() data type}.
     *
     * @return True if this display field's data type's values can be compared against empty quotes when specified in
     * SQL, false otherwise.
     */
    public boolean needsSQLEmptyQuotes() {
        return MapTypesForEmptyQuotes(getDataType());
    }

    /**
     * Indicates whether the display field is visible.
     *
     * @return Whether the display field is visible.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets whether the display field is visible.
     *
     * @param visible Whether the display field should be visible.
     */
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    /**
     * Sets whether the display field is visible.
     *
     * @param visible Whether the display field should be visible.
     */
    public void setVisible(String visible) {
        setVisible(Boolean.parseBoolean(visible));
    }

    /**
     * Returns the sort index.
     *
     * @return The sort index.
     */
    @SuppressWarnings("unused")
    public int getSortIndex() {
        return sortIndex;
    }

    /**
     * Sets the sort index for the display field.
     *
     * @param sortIndex The sort index to set.
     */
    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }

    /**
     * Gets the sequence. This is identical to the {@link #getSortIndex() sort index}.
     *
     * @return The sequence.
     * @deprecated Use {@link #getSortIndex()} instead.
     */
    @Deprecated
    public int getSequence() {
        return getSortIndex();
    }

    /**
     * Sets the sequence. This is identical to the {@link #setSortIndex(int)}  sort index}.
     *
     * @param sequence The sequence to set.
     * @deprecated Use {@link #setSortIndex(int)} instead.
     */
    @Deprecated
    public void setSequence(int sequence) {
        setSortIndex(sequence);
    }

    /**
     * Gets the enumeration from the display field, if available.
     *
     * @param login The login to authorize.
     * @return A list of any enumeration values the user can see.
     */
    public List<String> getEnumeration(String login) {
        if (this.elements.size() == 1) {
            DisplayFieldElement dfe = elements.get(0);
            try {
                SchemaField sf = dfe.getSchemaField();
                //noinspection unchecked
                possibleValues.addAll(sf.getPossibleValues(login).values());
            } catch (XFTInitException e) {
            logger.error("Error initializing XFT", e);
            } catch (ElementNotFoundException e) {
                logger.error("Couldn't find the requested element " + e.ELEMENT, e);
            } catch (Exception e) {
                logger.error("An unknown exception occurred", e);
            }
        }
        return new ArrayList<>(possibleValues);
    }

    public List<String> getEnumeration() {
        return getEnumeration(null);
    }

    public String getLightColor() {
        return this.getParentDisplay().getLightColor();
    }

    public String getDarkColor() {
        return this.getParentDisplay().getDarkColor();
    }

    public String toString() {
        return this.getParentDisplay().getElementName() + ":" + this.getId();
    }

    /**
     * @return Returns the htmlContent.
     */
    public boolean isHtmlContent() {
        return htmlContent;
    }

    /**
     * @param htmlContent The htmlContent to set.
     */
    public void setHtmlContent(boolean htmlContent) {
        this.htmlContent = htmlContent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static DisplayField getDisplayFieldForUnknownPath(String s) throws Exception {
        if (s.contains(".")) {
            String keyElement = s.substring(0, s.indexOf("."));
            String keyField = s.substring(s.indexOf(".") + 1);

            SchemaElement se = SchemaElement.GetElement(keyElement);
            return se.getDisplayField(keyField);
        } else {
            SchemaElement se = SchemaElement.GetElement(StringUtils.GetRootElementName(s));
            return se.getDisplayFieldForXMLPath(s);
        }
    }

    public static DisplayField getDisplayFieldForDFIdOrXPath(String s) throws Exception {
        final String elementName1 = StringUtils.GetRootElementName(s);
        final String field = StringUtils.GetFieldText(s);

        final SchemaElement element = SchemaElement.GetElement(elementName1);

        try {
            return element.getDisplayField(field);
        } catch (DisplayFieldNotFoundException e) {
            try {
                return element.getDisplayFieldForXMLPath(s);
            } catch (Exception e1) {
                logger.error("", e1);
                throw e;
            }
        }

    }

    private String deriveType() {
        if (this.elements.size() == 1) {
            DisplayFieldElement dfe = elements.get(0);
            try {
                return dfe.getFieldType();
            } catch (Exception ignored) {
            }
            return "UNKNOWN";
        } else {
            return "UNKNOWN";
        }
    }
}

