/*
 * automation: org.nrg.automation.event.entities.AutomationFilters
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.event.entities;

import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.framework.event.Filterable;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * The Class AutomationFilters.
 */
@Entity
@SuppressWarnings("serial")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"externalId", "srcEventClass", "field"}))
public class AutomationFilters extends AbstractHibernateEntity implements Serializable {

    /**
     * The external id.
     */
    private String externalId;

    /**
     * The src event class.
     */
    private String srcEventClass;

    /**
     * The field.
     */
    private String field;

    /**
     * The values.
     */
    private Set<String> values;

    /**
     * Instantiates a new automation filters.
     */
    public AutomationFilters() {
        super();
    }

    public AutomationFilters(final String externalId, final String srcEventClass, String field) {
        this();
        this.externalId = externalId;
        this.srcEventClass = srcEventClass;
        this.field = field;
        this.values = new HashSet<>();
    }

    /**
     * Sets the external id.
     *
     * @param externalId the new external id
     */
    @SuppressWarnings("unused")
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    /**
     * Gets the external id.
     *
     * @return the external id
     */
    @SuppressWarnings("unused")
    public String getExternalId() {
        return this.externalId;
    }

    /**
     * Sets the src event class.
     *
     * @param srcEventClass the new src event class
     */
    public void setSrcEventClass(String srcEventClass) {
        this.srcEventClass = srcEventClass;
    }

    /**
     * Gets the src event class.
     *
     * @return the src event class
     */
    public String getSrcEventClass() {
        return this.srcEventClass;
    }

    /**
     * Gets the field.
     *
     * @return the field
     */
    public String getField() {
        return field;
    }

    /**
     * Sets the field.
     *
     * @param field the new field
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     * Gets the values.
     *
     * @return the values
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "\"values\"") // Required to work with H2 for unit tests
    @CollectionTable
    public Set<String> getValues() {
        return new HashSet<>(values);
    }

    /**
     * Sets the values.
     *
     * @param values the new values
     */
    public void setValues(Set<String> values) {
        this.values = values;
    }

    @Transient
    public boolean addValue(final String value) {
        return values.add(value);
    }

    @Override
    public String toString() {
        return "AutomationFilters{" +
               "externalId='" + externalId + "'" +
               ", srcEventClass='" + srcEventClass + "'" +
               ", field='" + field + "'" +
               ", values=" + StringUtils.join(values, ", ") + "}";
    }
}
