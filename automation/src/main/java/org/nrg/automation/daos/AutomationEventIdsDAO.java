/*
 * automation: org.nrg.automation.daos.AutomationEventIdsDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.daos;

import org.nrg.automation.event.entities.AutomationEventIds;
import org.nrg.framework.orm.hibernate.QueryBuilder;
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
    public AutomationEventIds getEventIdsByProjectIdAndSrcEventClass(String projectId, String srcEventClass, boolean exactMatchExternalId) {
        QueryBuilder<AutomationEventIds> queryBuilder = newQueryBuilder();
        queryBuilder.getQuery()
                    .select(queryBuilder.getRoot())
                    .where(queryBuilder.and(getProjectRestriction(queryBuilder, projectId, exactMatchExternalId),
                                            queryBuilder.eq("srcEventClass", srcEventClass)));
        return queryBuilder.getResult().orElse(null);
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
    public List<AutomationEventIds> getEventIds(String projectId, String srcEventClass, boolean exactMatchExternalId) {
        QueryBuilder<AutomationEventIds> queryBuilder = newQueryBuilder();
        queryBuilder.getQuery()
                .select(queryBuilder.getRoot())
                .where(queryBuilder.and(getProjectRestriction(queryBuilder, projectId, exactMatchExternalId),
                        queryBuilder.eq("srcEventClass", srcEventClass)));
        return queryBuilder.getResults();
    }

    /**
     * Gets the event ids.
     *
     * @param projectId            the project id
     * @param exactMatchExternalId the exact match external id
     *
     * @return the event ids
     */
    public List<AutomationEventIds> getEventIds(String projectId, boolean exactMatchExternalId) {
        QueryBuilder<AutomationEventIds> queryBuilder = newQueryBuilder();
        queryBuilder.getQuery()
                    .select(queryBuilder.getRoot())
                    .where(queryBuilder.and(getProjectRestriction(queryBuilder, projectId, exactMatchExternalId)));
        return queryBuilder.getResults();
    }
}
