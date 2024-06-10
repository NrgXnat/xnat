/*
 * web: org.nrg.xnat.tracking.services.impl.EventTrackingDataServiceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2020, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.tracking.services.impl;

import com.google.common.util.concurrent.Striped;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.tracking.entities.EventTrackingDataPojo;
import org.nrg.xnat.tracking.model.TrackableEvent;
import org.nrg.xnat.tracking.services.EventTrackingDataHibernateService;
import org.nrg.xnat.tracking.services.EventTrackingDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.concurrent.locks.Lock;

@Slf4j
@Service
public class EventTrackingDataServiceImpl implements EventTrackingDataService {
    private final EventTrackingDataHibernateService eventTrackingDataHibernateService;
    private static final int NUM_LOCKS = 256;

    @SuppressWarnings("UnstableApiUsage")  // Striped is marked as @Beta in our guava version, but not in current version
    private static final Striped<Lock> LOCKS = Striped.lazyWeakLock(NUM_LOCKS);

    @Autowired
    public EventTrackingDataServiceImpl(final EventTrackingDataHibernateService eventTrackingDataHibernateService) {
        this.eventTrackingDataHibernateService = eventTrackingDataHibernateService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPayloadByKey(final String key, UserI user) throws NotFoundException {
        return getPojoByKey(key, user).getPayload();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventTrackingDataPojo getPojoByKey(String key, UserI user) throws NotFoundException {
        return eventTrackingDataHibernateService.findByKey(key, user.getID()).toPojo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createOrRestartWithKey(String key, UserI user) throws IllegalAccessException {
        eventTrackingDataHibernateService.createOrRestartWithKey(key, user.getID());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createOrUpdate(TrackableEvent eventData) throws IllegalAccessException {
        @SuppressWarnings("UnstableApiUsage")
        final Lock lock = LOCKS.get(eventData.getTrackingId());
        lock.lock();
        try {
            eventTrackingDataHibernateService.createOrUpdate(eventData);
        } finally {
            lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanupOldEntries() {
        // Remove entries >1 month old
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        eventTrackingDataHibernateService.deleteEntriesOlderThan(cal.getTime());
    }
}
