/*
 * automation: org.nrg.automation.daos.AutomationEventIdsIdsDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.daos;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.automation.event.entities.AutomationEventIds;
import org.nrg.automation.event.entities.AutomationEventIdsIds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The Class AutomationEventIdsDAO.
 */
@Repository
@Slf4j
public class AutomationEventIdsIdsDAO extends AutomationEntitiesDAO<AutomationEventIdsIds> {
    private final AutomationEventIdsDAO _automationEventIdsIdsDAO;

    @Autowired
    public AutomationEventIdsIdsDAO(final AutomationEventIdsDAO automationEventIdsDAO) {
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
    public List<AutomationEventIdsIds> getEventIds(String projectId, String srcEventClass, String eventId, boolean exactMatchExternalId) {
        CriteriaBuilder                      criteriaBuilder = getCriteriaBuilder();
        CriteriaQuery<AutomationEventIdsIds> criteriaQuery   = criteriaBuilder.createQuery(AutomationEventIdsIds.class);

        Root<AutomationEventIdsIds>                     root       = criteriaQuery.from(AutomationEventIdsIds.class);
        Join<AutomationEventIdsIds, AutomationEventIds> join       = root.join("parentAutomationEventIds", JoinType.LEFT);
        final List<Predicate>                           predicates = new ArrayList<>();
        if (StringUtils.isNotBlank(projectId)) {
            predicates.add(getProjectRestriction(criteriaBuilder, join, projectId, exactMatchExternalId));
        }
        if (StringUtils.isNotBlank(srcEventClass)) {
            predicates.add(criteriaBuilder.equal(join.get("srcEventClass"), srcEventClass));
        }
        if (StringUtils.isNotBlank(eventId)) {
            predicates.add(criteriaBuilder.equal(root.get("eventId"), eventId));
        }
        return createQuery(criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])))).getResultList();
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
    public List<AutomationEventIdsIds> getEventIds(String projectId, String srcEventClass, boolean exactMatchExternalId) {
        return getEventIds(projectId, srcEventClass, null, exactMatchExternalId);
    }

    /**
     * Gets the event ids.
     *
     * @param projectId            the project id
     * @param exactMatchExternalId the exact match external id
     *
     * @return the event ids
     */
    public List<AutomationEventIdsIds> getEventIds(String projectId, boolean exactMatchExternalId) {
        return getEventIds(projectId, null, null, exactMatchExternalId);
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
        return trimList(getEventIds(projectId, exactMatchExternalId), maxPerType);
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
        final Iterator<AutomationEventIdsIds> i       = idsList.iterator();
        AutomationEventIdsIds                 prevIds = null;
        int                                   counter = 1;
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
}
