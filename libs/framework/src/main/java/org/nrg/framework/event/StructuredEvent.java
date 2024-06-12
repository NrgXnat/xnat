/*
 * framework: org.nrg.framework.event.StructuredEvent
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.event;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.nrg.framework.event.StructuredEventI;
import org.nrg.framework.event.entities.EventSpecificFields;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * The Abstract Class StructuredEvent.
 * 
 * This class provides an optional implementation for the StructuredEventI interface.  That interface defines 
 * fields important for both persistent events and those that trigger automation scripts.  
 */
public abstract class StructuredEvent implements Serializable, StructuredEventI {
    
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5252326449503618313L;
	
	/** The src event class. */
	private String srcEventClass;
	
	/** The event id. */
	private String eventId;
	
	/** The user id. */
	private Integer userId;
	
	/** The external id. */
	private String externalId;
	
	/** The entity id. */
	private String entityId;
	
	/** The entity type. */
	private String entityType;
	
	/** The src stringified id. */
	private String srcStringifiedId;
	
	/** The event specific fields. */
	private Set<EventSpecificFields> eventSpecificFields;
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.event.StructuredEventI#setEventId(java.lang.String)
	 */
	public void setEventId(String eventId) {
        this.eventId = eventId;
    }

	/* (non-Javadoc)
	 * @see org.nrg.xft.event.StructuredEventI#getEventId()
	 */
	public String getEventId() {
        return this.eventId;
    }
    
	/* (non-Javadoc)
	 * @see org.nrg.xft.event.StructuredEventI#setUserId(java.lang.Integer)
	 */
	public void setUserId(Integer userId) {
        this.userId = userId;
    }

	/* (non-Javadoc)
	 * @see org.nrg.xft.event.StructuredEventI#getUserId()
	 */
	public Integer getUserId() {
        return this.userId;
    }

	/* (non-Javadoc)
	 * @see org.nrg.xft.event.StructuredEventI#setExternalId(java.lang.String)
	 */
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.event.StructuredEventI#getExternalId()
	 */
	public String getExternalId() {
		return this.externalId;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.event.StructuredEventI#setEntityId(java.lang.String)
	 */
	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.event.StructuredEventI#getEntityId()
	 */
	public String getEntityId() {
		return this.entityId;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.event.StructuredEventI#setEntityType(java.lang.String)
	 */
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.event.StructuredEventI#getEntityType()
	 */
	public String getEntityType() {
		return this.entityType;
	}

	/**
	 * Sets the src stringified id.
	 *
	 * @param srcStringifiedId the new src stringified id
	 */
	public void setSrcStringifiedId(String srcStringifiedId) {
		this.srcStringifiedId = srcStringifiedId;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.event.StructuredEventI#getSrcStringifiedId()
	 */
	public String getSrcStringifiedId() {
		return this.srcStringifiedId;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.event.StructuredEventI#setSrcEventClass(java.lang.String)
	 */
	public void setSrcEventClass(String srcEventClass) {
		this.srcEventClass = srcEventClass;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.event.StructuredEventI#getSrcEventClass()
	 */
	public String getSrcEventClass() {
		return this.srcEventClass;
	}
	
	/**
	 * Gets the event specific fields.
	 *
	 * @return the event specific fields
	 */
	public Set<EventSpecificFields> getEventSpecificFields() {
		return eventSpecificFields;
	}

	/**
	 * Sets the event specific fields.
	 *
	 * @param eventSpecificFields the new event specific fields
	 */
	public void setEventSpecificFields(Set<EventSpecificFields> eventSpecificFields) {
		this.eventSpecificFields = eventSpecificFields;
	}

	/**
	 * Sets the event specific fields as map.
	 *
	 * @param eventSpecificMap the event specific map
	 */
	public void setEventSpecificFieldsAsMap(Map<String,String> eventSpecificMap) {
		final Set<EventSpecificFields> eventSpecificFields = Sets.newHashSet();
    	for (final String eventKey : eventSpecificMap.keySet()) {
    		final EventSpecificFields ef = new EventSpecificFields(eventKey,eventSpecificMap.get(eventKey));
    		eventSpecificFields.add(ef);
    	}
		this.eventSpecificFields = eventSpecificFields;
	}
	
	/**
	 * Gets the event specific fields as map.
	 *
	 * @return the event specific fields as map
	 */
	public Map<String,String> getEventSpecificFieldsAsMap() {
		final Map<String,String> eventSpecificFieldMap = Maps.newHashMap();
		for (final EventSpecificFields fields : eventSpecificFields) {
			eventSpecificFieldMap.put(fields.getFieldName(), fields.getFieldVal());
		}
		return eventSpecificFieldMap;
	}

}
