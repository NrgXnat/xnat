package org.nrg.xft.event.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xft.event.AutomationEventImplementerI;

import com.google.common.collect.Lists;

/**
 * The Class AutomationEventIds.
 */
@Entity
@SuppressWarnings("serial")
//@Table(uniqueConstraints=@UniqueConstraint(columnNames = { "externalId", "srcEventClass","eventId"}))
@Table(uniqueConstraints=@UniqueConstraint(columnNames = { "externalId", "srcEventClass" }))
public class AutomationEventIds extends AbstractHibernateEntity implements Serializable {
	
	/**
	 * Instantiates a new automation event ids.
	 */
	public AutomationEventIds() {
		super();
	}
	
	/**
	 * Instantiates a new automation event ids.
	 *
	 * @param eventData the event data
	 */
	public AutomationEventIds(AutomationEventImplementerI eventData) {
		this();
		this.externalId = eventData.getExternalId();
		this.srcEventClass = eventData.getSrcEventClass();
		this.eventIds = Lists.newArrayList();
		this.eventIds.add(eventData.getEventId());
		//this.eventId = eventData.getEventId();
	}
    
	/** The external id. */
	private String externalId;
	
	/** The src event class. */
	private String srcEventClass;
	
	/** The event ids. */
	private List<String> eventIds;
	//private String eventId;
	
	/**
	 * Sets the external id.
	 *
	 * @param externalId the new external id
	 */
	/*
	public void setEventId(String eventId) {
        this.eventId = eventId;
    }

	public String getEventId() {
        return this.eventId;
    }
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
	 * Sets the event ids.
	 *
	 * @param eventIds the new event ids
	 */
	public void setEventIds(List<String> eventIds) {
        this.eventIds = eventIds;
    }

    /**
     * Gets the event ids.
     *
     * @return the event ids
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable
	public List<String> getEventIds() {
        return this.eventIds;
    }
    
}
