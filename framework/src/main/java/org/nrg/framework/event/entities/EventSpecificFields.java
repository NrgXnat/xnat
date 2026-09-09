/*
 * framework: org.nrg.framework.event.entities.EventSpecificFields
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.event.entities;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.io.Serializable;

// TODO: Auto-generated Javadoc

/**
 * The Class EventSpecificFields.
 */
@SuppressWarnings("unused")
@Entity
@Cacheable
public class EventSpecificFields implements Serializable {
    private static final long serialVersionUID = -3032912730958594504L;

    private String _fieldName;
    private String _fieldVal;
    private Long _id;

    /**
     * Instantiates a new event specific fields.
     */
    public EventSpecificFields() {
    }

    /**
     * Instantiates a new event specific fields.
     *
     * @param fieldName the field name
     * @param fieldVal  the field val
     */
    public EventSpecificFields(final String fieldName, final String fieldVal) {
        _fieldName = fieldName;
        _fieldVal  = fieldVal;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return _id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(Long id) {
        _id = id;
    }

    /**
     * Gets the field name.
     *
     * @return the field name
     */
    public String getFieldName() {
        return _fieldName;
    }

    /**
     * Sets the field name.
     *
     * @param fieldName the new field name
     */
    public void setFieldName(final String fieldName) {
        _fieldName = fieldName;
    }

    /**
     * Gets the field val.
     *
     * @return the field val
     */
    public String getFieldVal() {
        return _fieldVal;
    }

    /**
     * Sets the field val.
     *
     * @param fieldVal the new field val
     */
    public void setFieldVal(final String fieldVal) {
        _fieldVal = fieldVal;
    }
}
