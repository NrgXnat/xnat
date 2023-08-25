/*
 * automation: org.nrg.automation.event.entities.AutomationFilters
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.event.entities;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;
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
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    /**
     * Gets the external id.
     *
     * @return the external id
     */
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
