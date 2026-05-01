/*
 * automation: org.nrg.automation.services.impl.hibernate.HibernateAutomationEventIdsService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.services.impl.hibernate;

import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.automation.daos.AutomationEventIdsDAO;
import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.automation.event.entities.AutomationEventIds;
import org.nrg.automation.services.AutomationEventIdsService;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class HibernateAutomationEventIdsService.
 */
@Service
@Slf4j
public class HibernateAutomationEventIdsService extends AbstractHibernateEntityService<AutomationEventIds, AutomationEventIdsDAO> implements AutomationEventIdsService {
    /* (non-Javadoc)
     * @see org.nrg.automation.services.AutomationEventIdsService#saveOrUpdate(org.nrg.automation.event.entities.AutomationEventIds)
     */
    @Override
    @Transactional
    public void saveOrUpdate(AutomationEventIds e) {
        getDao().saveOrUpdate(e);
    }

    /* (non-Javadoc)
     * @see org.nrg.automation.services.AutomationEventIdsService#getEventIds(java.lang.String, java.lang.String, boolean)
     */
    @Override
    @Transactional
    public AutomationEventIds getEventIdsByProjectIdAndSrcEventClass(String projectId, String srcEventClass, boolean exactMatchExternalId) {
        return getDao().getEventIdsByProjectIdAndSrcEventClass(projectId, srcEventClass, exactMatchExternalId);
    }

    @Override
    @Transactional
    public List<AutomationEventIds> getEventIds(String projectId, String srcEventClass, boolean exactMatchExternalId) {
        return getDao().getEventIds(projectId, srcEventClass, exactMatchExternalId);
    }

    /* (non-Javadoc)
     * @see org.nrg.automation.services.AutomationEventIdsService#getEventIds(java.lang.String, boolean)
     */
    @Override
    @Transactional
    public List<AutomationEventIds> getEventIds(String projectId, boolean exactMatchExternalId) {
        return getDao().getEventIds(projectId, exactMatchExternalId);
    }

    /* (non-Javadoc)
     * @see org.nrg.automation.services.AutomationEventIdsService#getEventIds(org.nrg.automation.event.AutomationEventImplementerI)
     */
    @Override
    @Transactional
    public List<AutomationEventIds> getEventIds(AutomationEventImplementerI eventData) {
        final String externalId    = eventData.getExternalId();
        final String srcEventClass = eventData.getSrcEventClass();
        if (StringUtils.isNoneBlank(externalId, srcEventClass)) {
            return Collections.singletonList(getDao().getEventIdsByProjectIdAndSrcEventClass(externalId, srcEventClass, true));
        }
        if (StringUtils.isNotBlank(externalId)) {
            return getEventIds(externalId, true);
        }
        if (StringUtils.isNotBlank(srcEventClass)) {
            return getDao().findByProperty("srcEventClass", srcEventClass);
        }
        throw new IllegalArgumentException("The event data parameter must have at least one of external ID or source event class set to a non-blank value");
    }
}
