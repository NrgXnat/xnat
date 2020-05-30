package org.nrg.xnat.tracking.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.tracking.daos.EventTrackingDataDao;
import org.nrg.xnat.tracking.entities.EventTrackingData;
import org.nrg.xnat.tracking.model.TrackableEvent;
import org.nrg.xnat.tracking.services.EventTrackingDataHibernateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Service
public class EventTrackingDataHibernateServiceImpl
        extends AbstractHibernateEntityService<EventTrackingData, EventTrackingDataDao>
        implements EventTrackingDataHibernateService {

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public EventTrackingData createWithKey(String key) {
        return create(new EventTrackingData(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void createOrUpdate(TrackableEvent eventData) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public EventTrackingData findByKey(String key) throws NotFoundException {
        EventTrackingData eventTrackingData = getDao().findByUniqueProperty("key", key);
        if (eventTrackingData == null) {
            throw new NotFoundException("No event listener data with key " + key);
        }
        return eventTrackingData;
    }

    /**
     * Get EventListenerData entity by key
     * @param key the key
     * @return the entity
     */
    private EventTrackingData findOrCreateByKey(String key) {
        try {
            return findByKey(key);
        } catch (NotFoundException e) {
            return createWithKey(key);
        }
    }
}