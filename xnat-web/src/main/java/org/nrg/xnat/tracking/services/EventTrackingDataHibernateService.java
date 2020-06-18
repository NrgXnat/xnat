package org.nrg.xnat.tracking.services;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.tracking.entities.EventTrackingData;
import org.nrg.xnat.tracking.model.TrackableEvent;

public interface EventTrackingDataHibernateService extends BaseHibernateService<EventTrackingData> {
    /**
     * Create new EventListenerData entity with key
     * @param key the key
     * @param userId the user
     * @return the entity
     */
    EventTrackingData createWithKey(String key, Integer userId);

    /**
     * Create or update eventTrackingData with TrackableEvent.
     *
     * @param eventData the trackable event
     * @throws IllegalAccessException if user cannot read this event tracking data
     */
    void createOrUpdate(TrackableEvent eventData) throws IllegalAccessException;

    /**
     * Find event tracking data by key
     * @param key the key
     * @param userId the user
     * @return the event tracking data
     * @throws NotFoundException if no event listener data exists for this key or user cannot access it
     */
    EventTrackingData findByKey(String key, Integer userId) throws NotFoundException;
}
