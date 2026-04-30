/*
 * automation: org.nrg.automation.services.impl.hibernate.HibernateAutomationEventIdsIdsService
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
import org.apache.commons.lang3.ObjectUtils;
import org.nrg.automation.daos.AutomationEventIdsIdsDAO;
import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.automation.event.entities.AutomationEventIds;
import org.nrg.automation.event.entities.AutomationEventIdsIds;
import org.nrg.automation.services.AutomationEventIdsIdsService;
import org.nrg.automation.services.AutomationEventIdsService;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.nrg.automation.services.AutomationService.AUTOMATION_ENABLED_PROPERTY;

/**
 * The Class HibernateAutomationEventIdsIdsService.
 */
@Service
@Transactional
@Slf4j
public class HibernateAutomationEventIdsIdsService extends AbstractHibernateEntityService<AutomationEventIdsIds, AutomationEventIdsIdsDAO> implements AutomationEventIdsIdsService {
    private final AutomationEventIdsService _eventIdsService;

    @Value(AUTOMATION_ENABLED_PROPERTY)
    private boolean _automationEnabled;

    @Autowired
    public HibernateAutomationEventIdsIdsService(final AutomationEventIdsService eventIdsService) {
        _eventIdsService = eventIdsService;
    }

    @Override
    public AutomationEventIdsIds newAutomationEventIdsIds(final AutomationEventImplementerI eventData) {
        if (!_automationEnabled) {
            return null;
        }
        return newAutomationEventIdsIds(eventData.getExternalId(), eventData.getSrcEventClass(), eventData.getEventId());
    }

    @Override
    public AutomationEventIdsIds newAutomationEventIdsIds(final String externalId, final String srcEventClass, final String eventId) {
        if (!_automationEnabled) {
            return null;
        }
        AutomationEventIds autoEventIds = ObjectUtils.getIfNull(_eventIdsService.getEventIdsByProjectIdAndSrcEventClass(externalId, srcEventClass, true), () -> new AutomationEventIds(externalId, srcEventClass));
        return create(new AutomationEventIdsIds(eventId, autoEventIds));
    }

    /* (non-Javadoc)
     * @see org.nrg.automation.services.AutomationEventIdsIdsService#saveOrUpdate(org.nrg.automation.event.entities.AutomationEventIdsIds)
     */
    @Override
    @Transactional
    public void saveOrUpdate(AutomationEventIdsIds e) {
        if (_automationEnabled) {
            getDao().saveOrUpdate(e);
        }
    }

    /* (non-Javadoc)
     * @see org.nrg.automation.services.AutomationEventIdsIdsService#getEventIds(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    @Transactional
    public List<AutomationEventIdsIds> getEventIds(String projectId, String srcEventClass, String eventId, boolean exactMatchExternalId) {
        return _automationEnabled ? getDao().getEventIds(projectId, srcEventClass, eventId, exactMatchExternalId) : Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see org.nrg.automation.services.AutomationEventIdsIdsService#getEventIds(java.lang.String, java.lang.String, boolean)
     */
    @Override
    @Transactional
    public List<AutomationEventIdsIds> getEventIds(String projectId, String srcEventClass, boolean exactMatchExternalId) {
        return _automationEnabled ? getDao().getEventIds(projectId, srcEventClass, exactMatchExternalId) : Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see org.nrg.automation.services.AutomationEventIdsIdsService#getEventIds(java.lang.String, boolean)
     */
    @Override
    @Transactional
    public List<AutomationEventIdsIds> getEventIds(String projectId, boolean exactMatchExternalId) {
        return _automationEnabled ? getDao().getEventIds(projectId, exactMatchExternalId) : Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see org.nrg.automation.services.AutomationEventIdsIdsService#getEventIds(java.lang.String, boolean, int)
     */
    @Override
    @Transactional
    public List<AutomationEventIdsIds> getEventIds(String projectId, boolean exactMatchExternalId, int max_per_type) {
        return _automationEnabled ? getDao().getEventIds(projectId, exactMatchExternalId, max_per_type) : Collections.emptyList();
    }
}
