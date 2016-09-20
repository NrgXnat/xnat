/*
 * org.nrg.automation.event.entities.AutomationEventIds
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.event.entities;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

/**
 * The Class AutomationEventIds.
 */
@Entity
@SuppressWarnings("serial")
@Table(uniqueConstraints=@UniqueConstraint(columnNames = { "externalId", "srcEventClass" }))
public class AutomationEventIds extends AbstractHibernateEntity implements Serializable, Comparable<AutomationEventIds> {
	
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
	}
	
	/**
	 * Instantiates a new automation event ids.
	 *
	 * @param externalId the external id
	 * @param srcEventClass the src event class
	 */
	public AutomationEventIds(String externalId, String srcEventClass) {
		this();
		this.externalId = externalId;
		this.srcEventClass = srcEventClass;
	}
    
	/** The external id. */
	private String externalId;
	
	/** The src event class. */
	private String srcEventClass;
	
	/** The automation event ids ids. */
	private Set<AutomationEventIdsIds> automationEventIdsIds;
	
	
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
	 * Gets the automation event ids ids.
	 *
	 * @return the automation event ids ids
	 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy="parentAutomationEventIds")
	public Set<AutomationEventIdsIds> getAutomationEventIdsIds() {
		return automationEventIdsIds;
	}

	/**
	 * Sets the automation event ids ids.
	 *
	 * @param automationEventIdsIds the new automation event ids ids
	 */
	public void setAutomationEventIdsIds(Set<AutomationEventIdsIds> automationEventIdsIds) {
		this.automationEventIdsIds = automationEventIdsIds;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AutomationEventIds o) {
		final int compare = this.getExternalId().compareTo(o.getExternalId());
		return (compare!=0) ? compare : getSrcEventClass().compareTo(o.getSrcEventClass());
	}

}
