/*
 * org.nrg.xdat.display.Arc
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */
package org.nrg.xdat.display;

import java.util.Hashtable;

/**
 * @author Tim
 */
public class Arc {
    private String name = null;
    private final Hashtable<String, String> commonFields = new Hashtable<>();

    /**
     * Gets the common fields.
     *
     * @return A table containing the common fields.
     */
    public Hashtable getCommonFields() {
        return commonFields;
    }

    /**
     * Gets the archive name.
     *
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
    @SuppressWarnings({"unused", "unchecked"})
    public void setCommonFields(Hashtable commonFields) {
        this.commonFields.clear();
        this.commonFields.putAll(commonFields);
    }

    /**
     * Sets the archive's name.
     *
     * @param name The name to set for the archive.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Add a common field.
     *
     * @param id         The ID of the field to add.
     * @param localField The local field name to add.
     */
    public void addCommonField(String id, String localField) {
        commonFields.put(id, localField);
    }

    /**
     * Gets the archive definition.
     *
     * @return Returns the archive definition.
     */
    @SuppressWarnings("unused")
    public ArcDefinition getArcDefinition() {
        return DisplayManager.GetInstance().getArcDefinition(getName());
    }

}

