/*
 * automation: org.nrg.automation.event.entities.AutomationEventIdsIds
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.event.entities;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.List;


/**
 * The Class AutomationEventIds.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class AutomationEventIdsIds extends AbstractHibernateEntity implements Serializable, Comparable<AutomationEventIdsIds> {

	/**
	 * Instantiates a new automation event ids.
	 */
	public AutomationEventIdsIds() {
		super();
	}

	public AutomationEventIdsIds(String externalId, String srcEventClass, String eventId, final List<AutomationEventIds> idsList) {
		final AutomationEventIds autoEventIds = idsList.size() > 0 ? idsList.get(0) : new AutomationEventIds(externalId, srcEventClass);
		this.setParentAutomationEventIds(autoEventIds);
		this.setEventId(eventId);
		this.setCounter(1L);
	}

    /**
     * Gets the event id.
     *
     * @return the event id
     */
    public String getEventId() {
		return _eventId;
	}

	/**
	 * Sets the event id.
	 *
	 * @param _eventId the new event id
	 */
	public void setEventId(String _eventId) {
		this._eventId = _eventId;
	}

	/**
	 * Gets the counter.
	 *
	 * @return the counter
	 */
	public Long getCounter() {
		return _counter;
	}

	/**
	 * Sets the counter.
	 *
	 * @param _counter the new counter
	 */
	public void setCounter(Long _counter) {
		this._counter = _counter;
	}
    
    /**
     * Gets the parent automation event ids.
     *
     * @return the parent automation event ids
     */
    @ManyToOne(cascade=CascadeType.ALL)
	public AutomationEventIds getParentAutomationEventIds() {
		return _parentAutomationEventIds;
	}

	/**
	 * Sets the parent automation event ids.
	 *
	 * @param parentAutomationEventIds the new parent automation event ids
	 */
	public void setParentAutomationEventIds(AutomationEventIds parentAutomationEventIds) {
		this._parentAutomationEventIds = parentAutomationEventIds;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AutomationEventIdsIds arg0) {
		// We're reverse sorting to pull in the most common event IDs 
		int compare = this.getParentAutomationEventIds().compareTo(arg0.getParentAutomationEventIds());
		if (compare!=0) {
			return compare;
		}
		compare = (Long.valueOf(arg0.getCounter() - this.getCounter())).intValue();
		if (compare!=0) {
			return compare;
		}
		return this.getEventId().compareTo(arg0.getEventId());
	}

    /** The _event id. */
    private String _eventId;
    
    /** The _counter. */
    private Long _counter;
    
    /** The _parent automation event ids. */
    private AutomationEventIds  _parentAutomationEventIds;
    
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3238141894438535134L;

}
