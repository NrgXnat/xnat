/*
 * automation: org.nrg.automation.daos.AutomationFiltersDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.daos;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.nrg.automation.event.entities.AutomationFilters;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class AutomationFiltersDAO.
 */
@Repository
public class AutomationFiltersDAO extends AutomationEntitiesDAO<AutomationFilters> {

    /**
     * Gets the automation filters.
     *
     * @param projectId            the external id
     * @param srcEventClass        the src event class
     * @param column               the column
     * @param exactMatchExternalId the exact match external id
     *
     * @return the automation filters
     */
    @SuppressWarnings("rawtypes")
    public AutomationFilters getAutomationFilters(String projectId, String srcEventClass, String column, boolean exactMatchExternalId) {
        final Criteria criteria = getCriteriaForType();
        addProjectRestrictions(criteria, projectId, exactMatchExternalId);
        criteria.add(Restrictions.eq("srcEventClass", srcEventClass));
        criteria.add(Restrictions.eq("field", column));
        List oList = criteria.list();
        if (oList.size() > 0 && oList.get(0) instanceof AutomationFilters) {
            return (AutomationFilters) oList.get(0);
        }
        return null;
    }

    /**
     * Gets the automation filters.
     *
     * @param projectId            the external id
     * @param srcEventClass        the src event class
     * @param exactMatchExternalId the exact match external id
     *
     * @return the automation filters
     */
    @SuppressWarnings("unchecked")
    public List<AutomationFilters> getAutomationFilters(String projectId, String srcEventClass, boolean exactMatchExternalId) {
        final Criteria criteria = getCriteriaForType();
        addProjectRestrictions(criteria, projectId, exactMatchExternalId);
        criteria.add(Restrictions.eq("srcEventClass", srcEventClass));
        return criteria.list();
    }

    /**
     * Gets the automation filters.
     *
     * @param projectId            the external id
     * @param exactMatchExternalId the exact match external id
     *
     * @return the automation filters
     */
    @SuppressWarnings("unchecked")
    public List<AutomationFilters> getAutomationFilters(String projectId, boolean exactMatchExternalId) {
        final Criteria criteria = getCriteriaForType();
        addProjectRestrictions(criteria, projectId, exactMatchExternalId);
        return criteria.list();
    }
}
