/*
 * web: org.nrg.xnat.tracking.services.EventTrackingDataService
 * XNAT http://www.xnat.org
 * Copyright (c) 2020, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.tracking.services;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.tracking.entities.EventTrackingDataPojo;
import org.nrg.xnat.tracking.entities.EventTrackingData;
import org.nrg.xnat.tracking.model.TrackableEvent;

/**
 * Provides information about the current host.
 */
public interface EventTrackingDataService extends BaseHibernateService<EventTrackingData> {
    /**
     * Get EventListenerData payload by key
     * @param key the key
     * @return the payload
     * @throws NotFoundException if no event listener data exists for this key
     */
    String getPayloadByKey(final String key) throws NotFoundException;

    /**
     * Get EventListenerData entity by key
     * @param key the key
     * @return the pojo
     * @throws NotFoundException if no event listener data exists for this key
     */
    EventTrackingDataPojo getPojoByKey(final String key) throws NotFoundException;

    /**
     * Get EventListenerData entity by key
     * @param key the key
     * @return the entity
     */
    EventTrackingData findOrCreateByKey(final String key);

    /**
     * Create new EventListenerData entity with key
     * @param key the key
     * @return the entity
     */
    EventTrackingData createWithKey(String key);

    /**
     * Create or update eventTrackingData with TrackableEvent.
     *
     * <strong>This should not be used across JVMs (e.g., EventTrackingData created on one,
     * and createOrUpdated on another) as there is no inter-JVM locking</strong>
     *
     * @param eventData the trackable event
     */
    void createOrUpdate(TrackableEvent eventData);
}
