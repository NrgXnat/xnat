/*
 * automation: org.nrg.automation.daos.AutomationEventIdsDAO
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
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class AutomationEventIdsDAO.
 */
@Repository
public class AutomationEventIdsDAO extends AutomationEntitiesDAO<AutomationEventIds> {
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
    public List<AutomationEventIds> getEventIds(String projectId, String srcEventClass, boolean exactMatchExternalId) {
        final Criteria criteria = getCriteriaForType();
        addProjectRestrictions(criteria, projectId, exactMatchExternalId);
        criteria.add(Restrictions.eq("srcEventClass", srcEventClass));
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
    public List<AutomationEventIds> getEventIds(String projectId, boolean exactMatchExternalId) {
        final Criteria criteria = getCriteriaForType();
        addProjectRestrictions(criteria, projectId, exactMatchExternalId);
        return criteria.list();
    }
}
