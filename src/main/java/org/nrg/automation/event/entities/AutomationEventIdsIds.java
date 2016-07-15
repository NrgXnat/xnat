package org.nrg.automation.event.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.automation.services.AutomationEventIdsService;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;


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
	
	/**
	 * Instantiates a new automation event ids ids.
	 *
	 * @param externalId the external id
	 * @param srcEventClass the src event class
	 * @param eventId the event id
	 * @param idsService the ids service
	 */
	public AutomationEventIdsIds(String externalId, String srcEventClass, String eventId, AutomationEventIdsService idsService) {
		List<AutomationEventIds> idsList = idsService .getEventIds(externalId, srcEventClass, true);
		AutomationEventIds autoEventIds;
		if (idsList.size()>0) {
			autoEventIds = idsList.get(0); 
		} else {
			autoEventIds = new AutomationEventIds(externalId, srcEventClass);
			autoEventIds.setCreated(new Date());
		}
		this.setParentAutomationEventIds(autoEventIds);
		this.setEventId(eventId);
		this.setCounter(Long.valueOf(1));
	}
    
	/**
	 * Instantiates a new automation event ids ids.
	 *
	 * @param eventData the event data
	 * @param idsService the ids service
	 */
	public AutomationEventIdsIds(AutomationEventImplementerI eventData, AutomationEventIdsService idsService) {
		this(eventData.getExternalId(), eventData.getSrcEventClass(), eventData.getEventId(), idsService);
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
