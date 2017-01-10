/*
 * automation: org.nrg.automation.daos.AutomationEventIdsIdsDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.daos;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.automation.event.entities.AutomationEventIds;
import org.nrg.automation.event.entities.AutomationEventIdsIds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The Class AutomationEventIdsDAO.
 */
@Repository
public class AutomationEventIdsIdsDAO extends AutomationEntitiesDAO<AutomationEventIdsIds> {

    @Autowired
    public void setAutomationEventIdsDAO(final AutomationEventIdsDAO automationEventIdsDAO) {
        _automationEventIdsIdsDAO = automationEventIdsDAO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable create(final AutomationEventIdsIds entity) {
        checkParentEventId(entity);
        return super.create(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final AutomationEventIdsIds entity) {
        checkParentEventId(entity);
        super.update(entity);
    }

    /**
     * Gets the event ids.
     *
     * @param projectId            the project id
     * @param srcEventClass        the src event class
     * @param eventId              the event id
     * @param exactMatchExternalId the exact match external id
     *
     * @return the event ids
     */
    @SuppressWarnings("unchecked")
    public List<AutomationEventIdsIds> getEventIds(String projectId, String srcEventClass, String eventId, boolean exactMatchExternalId) {
        final Criteria criteria = getCriteriaForType();
        criteria.createAlias("parentAutomationEventIds", "parentAutomationEventIds");
        addProjectRestrictions(criteria, projectId, exactMatchExternalId, "parentAutomationEventIds.externalId");
        criteria.add(Restrictions.eq("parentAutomationEventIds.srcEventClass", srcEventClass));
        criteria.add(Restrictions.eq("eventId", eventId));
        return criteria.list();
    }

    /**
     * Gets the event ids.
     *
     * @param projectId            the project id
     * @param srcEventClass        the src event class
     * @param exactMatchExternalId the exact match external id
     *
     * @return the event ids
     */
    @SuppressWarnings("unchecked")
    public List<AutomationEventIdsIds> getEventIds(String projectId, String srcEventClass, boolean exactMatchExternalId) {
        final Criteria criteria = getCriteriaForType();
        criteria.createAlias("parentAutomationEventIds", "parentAutomationEventIds");
        addProjectRestrictions(criteria, projectId, exactMatchExternalId, "parentAutomationEventIds.externalId");
        criteria.add(Restrictions.eq("parentAutomationEventIds.srcEventClass", srcEventClass));
        return criteria.list();
    }

    /**
     * Gets the event ids.
     *
     * @param projectId            the project id
     * @param exactMatchExternalId the exact match external id
     *
     * @return the event ids
     */
    @SuppressWarnings("unchecked")
    public List<AutomationEventIdsIds> getEventIds(String projectId, boolean exactMatchExternalId) {
        final Criteria criteria = getCriteriaForType();
        criteria.createAlias("parentAutomationEventIds", "parentAutomationEventIds");
        addProjectRestrictions(criteria, projectId, exactMatchExternalId, "parentAutomationEventIds.externalId");
        return criteria.list();
    }

    /**
     * Gets the event ids.
     *
     * @param projectId            the project id
     * @param exactMatchExternalId the exact match external id
     * @param maxPerType           the maxPerType
     *
     * @return the event ids
     */
    public List<AutomationEventIdsIds> getEventIds(String projectId, boolean exactMatchExternalId, int maxPerType) {
        final List<AutomationEventIdsIds> idsList = getEventIds(projectId, exactMatchExternalId);
        return trimList(idsList, maxPerType);
    }

    /**
     * Trim list.
     *
     * @param idsList    the ids list
     * @param maxPerType the maxPerType
     *
     * @return the list
     */
    private List<AutomationEventIdsIds> trimList(final List<AutomationEventIdsIds> idsList, final int maxPerType) {
        Collections.sort(idsList);
        final Iterator<AutomationEventIdsIds> i = idsList.iterator();
        AutomationEventIdsIds prevIds = null;
        int counter = 1;
        while (i.hasNext()) {
            final AutomationEventIdsIds ids = i.next();
            counter = (prevIds == null || !prevIds.getParentAutomationEventIds().equals(ids.getParentAutomationEventIds())) ? 1 : counter + 1;
            if (counter > maxPerType) {
                i.remove();
            }
            prevIds = ids;
        }
        return idsList;
    }

    private void checkParentEventId(final AutomationEventIdsIds entity) {
        final AutomationEventIds parentAutomationEventIds = entity.getParentAutomationEventIds();
        if (parentAutomationEventIds != null && parentAutomationEventIds.getCreated() == null) {
            _automationEventIdsIdsDAO.create(parentAutomationEventIds);
        }
    }

    private AutomationEventIdsDAO _automationEventIdsIdsDAO;
}
