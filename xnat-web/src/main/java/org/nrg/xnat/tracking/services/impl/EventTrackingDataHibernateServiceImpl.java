package org.nrg.xnat.tracking.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.tracking.daos.EventTrackingDataDao;
import org.nrg.xnat.tracking.entities.EventTrackingData;
import org.nrg.xnat.tracking.model.TrackableEvent;
import org.nrg.xnat.tracking.services.EventTrackingDataHibernateService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EventTrackingDataHibernateServiceImpl extends AbstractHibernateEntityService<EventTrackingData, EventTrackingDataDao> implements EventTrackingDataHibernateService {

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public EventTrackingData createOrRestartWithKey(String key, Integer userId) throws IllegalAccessException {
        EventTrackingData eventTrackingData = findOrCreateByKey(key, userId);
        if (eventTrackingData.getSucceeded() != null) {
            // we already tracked an event with this key (this can happen if data fails to autoarchive and is left in
            // prearchive, then another attempt to autoarchive is made), clear its completion
            eventTrackingData.setSucceeded(null);
            eventTrackingData.setFinalMessage(null);
            update(eventTrackingData);
        }
        return eventTrackingData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void createOrUpdate(TrackableEvent eventData) throws IllegalAccessException {
        String key = eventData.getTrackingId();
        Integer userId = eventData.getUserId();
        EventTrackingData eventTrackingData = findOrCreateByKey(key, userId);
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
    public EventTrackingData findByKey(String key, Integer userId) throws NotFoundException {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("key", key);
        properties.put("userId", userId);
        List<EventTrackingData> eventTrackingDataList = getDao().findByProperties(properties);
        if (eventTrackingDataList == null || eventTrackingDataList.isEmpty()) {
            throw new NotFoundException("No event listener data with key " + key + " accessible to user");
        }
        if (eventTrackingDataList.size() > 1) {
            throw new RuntimeException("The specified key is not a unique constraint!");
        }
        return eventTrackingDataList.get(0);
    }

    /**
     * Get EventListenerData entity by key
     * @param key the key
     * @param userId the user
     * @return the entity
     * @throws IllegalAccessException if user cannot read this event tracking data
     */
    private EventTrackingData findOrCreateByKey(String key, Integer userId) throws IllegalAccessException {
        try {
            return findByKey(key, userId);
        } catch (NotFoundException e) {
            try {
                return create(new EventTrackingData(key, userId));
            } catch (DuplicateKeyException de) {
                // If we're trying to create with a key that's already in use, we're actually trying to update without
                // proper permission
                throw new IllegalAccessException("User cannot read event tracking for " + key);
            }
        }
    }
}
