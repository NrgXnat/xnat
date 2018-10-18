/*
 * core: org.nrg.xdat.display.ArcDefinition
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.display;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Tim
 */
public class ArcDefinition {
    private String name = null;
    private String bridgeElement = "";
    private String bridgeField = "";
    private final List<String[]> filters = new ArrayList<>();
    private final Map<String, String> commonFields = new Hashtable<>();
    private final List<String> members = new ArrayList<>();

    /**
     * Gets the common fields.
     *
     * @return A table containing the common fields.
     */
    public Map<String, String> getCommonFields() {
        return commonFields;
    }

    /**
     * Gets the archive filters.
     *
     * @return A list containing the archive filters.
     */
    public List<String[]> getFilters() {
        return filters;
    }

    /**
     * @return The bridge element.
     */
    public String getBridgeElement() {
        return bridgeElement;
    }

    /**
     * @return The archive name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the common fields.
     *
     * @param commonFields The common fields to be set.
     */
    @SuppressWarnings({"unused"})
    public void setCommonFields(final Map<String, String> commonFields) {
        this.commonFields.clear();
        this.commonFields.putAll(commonFields);
    }

    /**
     * Add a common field.
     *
     * @param id   The ID of the field to add.
     * @param type The field type to add.
     */
    public void addCommonField(String id, String type) {
        commonFields.put(id, type);
    }

    /**
     * Sets the list of filters for the archive definition.
     *
     * @param filters The list of filters to set.
     */
    public void setFilters(final List<String[]> filters) {
        this.filters.clear();
        this.filters.addAll(filters);
    }

    /**
     * Adds a filter.
     *
     * @param fieldID    The ID of the field to which the filter applies.
     * @param filterType The type of filter.
     */
    public void addFilter(String fieldID, String filterType) {
        filters.add(new String[]{fieldID, filterType});
    }

    /**
     * Sets the bridge element.
     *
     * @param bridgeElement The bridge element to set.
     */
    public void setBridgeElement(String bridgeElement) {
        this.bridgeElement = bridgeElement;
    }

    /**
     * Sets the archive definition name.
     *
     * @param name The name to set for the archive definition.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Iterator of ArrayList of ElementNames (String)
     *
     * @return A list of the members of the archive definition.
     */
    public Iterator<String> getMembers() {
        return members.iterator();
    }

    /**
     * Copy of the member list.
     *
     * @return A list of the members of the archive definition.
     */
    public List<String> getMemberList() {
        return new ArrayList<>(members);
    }

    /**
     * Adds the indicated member.
     *
     * @param elementName The element name to add.
     */
    public void addMember(String elementName) {
        members.add(elementName);
    }

    /**
     * Evaluates whether the indicated element is a member of the archive definition.
     *
     * @param elementName The element name to test.
     * @return True if the element is a member, false otherwise.
     */
    public boolean isMember(String elementName) {
        return members.contains(elementName);
    }

    /**
     * Gets the bridge field.
     *
     * @return The bridge field.
     */
    public String getBridgeField() {
        return bridgeField;
    }

    /**
     * Sets the bridge field to the indicated value.
     *
     * @param bridgeField The bridge field to set.
     */
    public void setBridgeField(final String bridgeField) {
        this.bridgeField = bridgeField;
    }

    public String getDistinctField() {
        String field = null;
        for (final String[] filter : this.filters) {
            if (filter[1].equalsIgnoreCase("distinct")) {
                field = filter[0];
                break;
            }
        }
        return field;
    }

    /**
     * Gets the field with an equals filter.
     *
     * @return The field with an equals filter if one exists, null otherwise.
     */
    public String getEqualsField() {
        String field = null;
        for (final String[] filter : this.filters) {
            if (filter[1].equalsIgnoreCase("equals")) {
                field = filter[0];
                break;
            }
        }
        return field;
    }

    /**
     * Gets the field with a closest filter.
     *
     * @return The field with a closest filter if one exists, null otherwise.
     */
    public String getClosestField() {
        String field = null;
        for (final String[] filter : this.filters) {
            if (filter[1].equalsIgnoreCase("closest")) {
                field = filter[0];
                break;
            }
        }
        return field;
    }

}

