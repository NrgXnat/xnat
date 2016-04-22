package org.nrg.xft.event.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * The Class EventSpecificFields.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class EventSpecificFields {

    /**
     * Instantiates a new event specific fields.
     */
    public EventSpecificFields() {
    }
    
    /**
     * Instantiates a new event specific fields.
     *
     * @param fieldName the field name
     * @param fieldVal the field val
     */
    public EventSpecificFields(final String fieldName, final String fieldVal) {
        _fieldName = fieldName;
        _fieldVal= fieldVal;
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

    /** The _field name. */
    private String _fieldName;
    
    /** The _field val. */
    private String _fieldVal;
    
    /** The _id. */
    private Long  _id;

}
