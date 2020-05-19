/*
 * web: org.nrg.xnat.services.system.impl.hibernate.HibernateHostInfoService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*
 * 
 */
package org.nrg.xnat.tracking.services.impl;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.tracking.daos.EventTrackingDataDao;
import org.nrg.xnat.tracking.entities.EventTrackingData;
import org.nrg.xnat.tracking.entities.EventTrackingDataPojo;
import org.nrg.xnat.tracking.services.EventTrackingDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@inheritDoc}
 */
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
    public EventTrackingData findByKey(String key) throws NotFoundException {
        EventTrackingData eventTrackingData = getDao().findByUniqueProperty("key", key);
        if (eventTrackingData == null) {
            throw new NotFoundException("No event listener data with key " + key);
        }
        return eventTrackingData;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public EventTrackingData findOrCreateByKey(String key) {
        try {
            return findByKey(key);
        } catch (NotFoundException e) {
            return create(new EventTrackingData(key));
        }
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
    public void createWithKey(String key) {
        create(new EventTrackingData(key));
    }
}
