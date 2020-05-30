package org.nrg.xnat.tracking.services;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.tracking.entities.EventTrackingData;
import org.nrg.xnat.tracking.model.TrackableEvent;

public interface EventTrackingDataHibernateService extends BaseHibernateService<EventTrackingData> {
    /**
     * Create new EventListenerData entity with key
     * @param key the key
     * @return the entity
     */
    EventTrackingData createWithKey(String key);

    /**
     * Create or update eventTrackingData with TrackableEvent.
     *
     * @param eventData the trackable event
     */
    void createOrUpdate(TrackableEvent eventData);

    /**
     * Find event tracking data by key
     * @param key the key
     * @return the event tracking data
     */
    EventTrackingData findByKey(String key) throws NotFoundException;
}
