/*
 * web: org.nrg.xnat.tracking.services.impl.EventTrackingDataServiceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2020, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.tracking.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.tracking.daos.EventTrackingDataDao;
import org.nrg.xnat.tracking.entities.EventTrackingData;
import org.nrg.xnat.tracking.entities.EventTrackingDataPojo;
import org.nrg.xnat.tracking.model.TrackableEvent;
import org.nrg.xnat.tracking.services.EventTrackingDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Service
public class EventTrackingDataServiceImpl
        extends AbstractHibernateEntityService<EventTrackingData, EventTrackingDataDao>
        implements EventTrackingDataService {

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public String getPayloadByKey(final String key) throws NotFoundException {
        return findByKey(key).getPayload();
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public EventTrackingDataPojo getPojoByKey(String key) throws NotFoundException {
        return findByKey(key).toPojo();
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public synchronized EventTrackingData findOrCreateByKey(String key) {
        try {
            return findByKey(key);
        } catch (NotFoundException e) {
            return createWithKey(key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public synchronized EventTrackingData createWithKey(String key) {
        return create(new EventTrackingData(key));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public synchronized void createOrUpdate(TrackableEvent eventData) {
        String key = eventData.getTrackingId();
        EventTrackingData eventTrackingData = findOrCreateByKey(key);
        if (eventData.isCompleted()) {
            eventTrackingData.setSucceeded(eventData.isSuccess());
            eventTrackingData.setFinalMessage(eventData.getMessage());
        } else {
            try {
                eventTrackingData.setPayload(eventData.updateTrackingPayload(eventTrackingData.getPayload()));
            } catch (IOException e) {
                log.error("Unable to parse payload, not updating event tracking data payload for {}", key, e);
            }
        }
        update(eventTrackingData);
    }

    private EventTrackingData findByKey(String key) throws NotFoundException {
        EventTrackingData eventTrackingData = getDao().findByUniqueProperty("key", key);
        if (eventTrackingData == null) {
            throw new NotFoundException("No event listener data with key " + key);
        }
        return eventTrackingData;
    }
}
