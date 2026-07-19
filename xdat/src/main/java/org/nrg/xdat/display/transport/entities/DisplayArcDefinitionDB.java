/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.display.transport.entities;

import com.google.common.collect.Maps;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import jakarta.persistence.*;
import java.util.*;

/**
 * @author Tim Olsen
 *
 * ArcDefinition objects that are storable via Hibernate
 *
 * ArcDefinitions are identify joining strategies between datatypes used by the search engine.
 */
@Entity
public class DisplayArcDefinitionDB extends AbstractHibernateEntity {
    private String name = null;
    private String bridgeElement = null;
    private String bridgeField = null;
    private List<DisplayArcDefFiltersDB>  filters;
    private Map<String, String> commonFields;

    /**
     * @return The arc definition name.
     */
    @Column(unique = true, nullable = false)
    public String getName() {
        return name;
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
     * @return The bridge element.
     */
    public String getBridgeElement() {
        return bridgeElement;
    }

    /**
     * Sets the bridge element.
     *
     * @param bridgeElement The bridge element to set.
     */
    public void setBridgeElement(final String bridgeElement) {
        this.bridgeElement = bridgeElement;
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

    /**
     * Gets the common fields.
     *
     * @return A table containing the common fields.
     */
    @ElementCollection()
    @MapKeyColumn(name = "field_id")
    @Column(name = "field_type")
    public Map<String, String> getCommonFields() {
        return commonFields;
    }

    /**
     * Sets the common fields.
     *
     * @param commonFields The common fields to be set.
     */
    @SuppressWarnings({"unused"})
    public void setCommonFields(final Map<String, String> commonFields) {
       this.commonFields=commonFields;
    }

    /**
     * Add a common field.
     *
     * @param id   The ID of the field to add.
     * @param type The field type to add.
     */
    @Transient
    public void addCommonField(final String id, final String type) {
        if(commonFields==null){
            commonFields=Maps.newHashMap();
        }
        commonFields.put(id, type);
    }


    /**
     * Gets the archive filters.
     *
     * @return A list containing the archive filters.
     */
    @OneToMany(cascade = {CascadeType.ALL})
    public List<DisplayArcDefFiltersDB> getFilters() {
        return filters;
    }

    /**
     * Sets the list of filters for the archive definition.
     *
     * @param filters The list of filters to set.
     */
    public void setFilters(final List<DisplayArcDefFiltersDB> filters) {
        this.filters=filters;
    }

}
