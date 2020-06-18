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
import org.nrg.xft.security.UserI;
import org.nrg.xnat.tracking.entities.EventTrackingDataPojo;
import org.nrg.xnat.tracking.model.TrackableEvent;
import org.nrg.xnat.tracking.services.EventTrackingDataHibernateService;
import org.nrg.xnat.tracking.services.EventTrackingDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventTrackingDataServiceImpl implements EventTrackingDataService {
    private final EventTrackingDataHibernateService eventTrackingDataHibernateService;

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
    public void createWithKey(String key, UserI user) {
        eventTrackingDataHibernateService.createWithKey(key, user.getID());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void createOrUpdate(TrackableEvent eventData) throws IllegalAccessException {
        eventTrackingDataHibernateService.createOrUpdate(eventData);
    }
}
