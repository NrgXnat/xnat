/*
 * automation: org.nrg.automation.event.entities.PersistentEvent
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.event.entities;

import org.nrg.framework.event.entities.EventSpecificFields;
import org.nrg.framework.event.persist.PersistentEventImplementerI;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class should be extended for all events that wish to be persistent.  It is a hibernate entity class which will be
 * persisted.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@SuppressWarnings("serial")
public abstract class PersistentEvent extends AbstractHibernateEntity implements PersistentEventImplementerI {

    /**
     * The src event class.
     */
    private String srcEventClass;

    /**
     * The event id.
     */
    private String eventId;

    /**
     * The user id.
     */
    private Integer userId;

    /**
     * The external id.
     */
    private String externalId;

    /**
     * The entity id.
     */
    private String entityId;

    /**
     * The entity type.
     */
    private String entityType;

    /**
     * The event specific fields.
     */
    private Set<EventSpecificFields> eventSpecificFields;

    /* (non-Javadoc)
     * @see org.nrg.xft.event.StructuredEventI#setEventId(java.lang.String)
     */
    @Override
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.event.StructuredEventI#getEventId()
     */
    @Override
    public String getEventId() {
        return this.eventId;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.event.StructuredEventI#setUserId(java.lang.Integer)
     */
    @Override
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.event.StructuredEventI#getUserId()
     */
    @Override
    public Integer getUserId() {
        return this.userId;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.event.StructuredEventI#setExternalId(java.lang.String)
     */
    @Override
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.event.StructuredEventI#getExternalId()
     */
    @Override
    public String getExternalId() {
        return this.externalId;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.event.StructuredEventI#setEntityId(java.lang.String)
     */
    @Override
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.event.StructuredEventI#getEntityId()
     */
    @Override
    public String getEntityId() {
        return this.entityId;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.event.StructuredEventI#setEntityType(java.lang.String)
     */
    @Override
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.event.StructuredEventI#getEntityType()
     */
    @Override
    public String getEntityType() {
        return this.entityType;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.event.StructuredEventI#getSrcStringifiedId()
     */
    @Override
    @Transient
    public String getSrcStringifiedId() {
        return String.valueOf(this.getId());
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.event.StructuredEventI#setSrcEventClass(java.lang.String)
     */
    @Override
    public void setSrcEventClass(String srcEventClass) {
        this.srcEventClass = srcEventClass;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.event.StructuredEventI#getSrcEventClass()
     */
    @Override
    public String getSrcEventClass() {
        return this.srcEventClass;
    }

    /**
     * Gets the event specific fields.
     *
     * @return the event specific fields
     */
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "id")
    public Set<EventSpecificFields> getEventSpecificFields() {
        return eventSpecificFields;
    }

    /**
     * Sets the event specific fields.
     *
     * @param eventSpecificFields the new event specific fields
     */
    @SuppressWarnings("unused")
    public void setEventSpecificFields(Set<EventSpecificFields> eventSpecificFields) {
        this.eventSpecificFields = eventSpecificFields;
    }

    /**
     * Sets the event specific fields as map.
     *
     * @param eventSpecificMap the event specific map
     */
    @SuppressWarnings("unused")
    public void setEventSpecificFieldsAsMap(Map<String, String> eventSpecificMap) {
        this.eventSpecificFields = eventSpecificMap.entrySet().stream()
                                                   .map(entry -> new EventSpecificFields(entry.getKey(), entry.getValue()))
                                                   .collect(Collectors.toSet());
    }

    /**
     * Gets the event specific fields as map.
     *
     * @return the event specific fields as map
     */
    @SuppressWarnings("unused")
    @Transient
    public Map<String, String> getEventSpecificFieldsAsMap() {
        return eventSpecificFields.stream().collect(Collectors.toMap(EventSpecificFields::getFieldName, EventSpecificFields::getFieldVal));
    }

}
