/*
 * automation: org.nrg.automation.daos.AutomationFiltersDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.daos;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.automation.event.entities.AutomationFilters;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class AutomationFiltersDAO.
 */
@Repository
@Slf4j
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
    public AutomationFilters getAutomationFilters(String projectId, String srcEventClass, String column, boolean exactMatchExternalId) {
        List<AutomationFilters> filters = getAutomationFiltersImpl(projectId, srcEventClass, column, exactMatchExternalId);
        return filters == null || filters.isEmpty() ? null : filters.get(0);
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
    public List<AutomationFilters> getAutomationFilters(String projectId, String srcEventClass, boolean exactMatchExternalId) {
        return getAutomationFiltersImpl(projectId, srcEventClass, null, exactMatchExternalId);
    }

    /**
     * Gets the automation filters.
     *
     * @param projectId            the external id
     * @param exactMatchExternalId the exact match external id
     *
     * @return the automation filters
     */
    public List<AutomationFilters> getAutomationFilters(String projectId, boolean exactMatchExternalId) {
        return getAutomationFiltersImpl(projectId, null, null, exactMatchExternalId);
    }

    private List<AutomationFilters> getAutomationFiltersImpl(String projectId, String srcEventClass, String column, boolean exactMatchExternalId) {
        CriteriaBuilder                  criteriaBuilder = getCriteriaBuilder();
        CriteriaQuery<AutomationFilters> criteriaQuery   = criteriaBuilder.createQuery(AutomationFilters.class);

        Root<AutomationFilters> root       = criteriaQuery.from(AutomationFilters.class);
        final List<Predicate>   predicates = new ArrayList<>();
        predicates.add(getProjectRestriction(criteriaBuilder, root, projectId, exactMatchExternalId));
        if (StringUtils.isNotBlank(srcEventClass)) {
            predicates.add(criteriaBuilder.equal(root.get("srcEventClass"), srcEventClass));
        }
        if (StringUtils.isNotBlank(column)) {
            predicates.add(criteriaBuilder.equal(root.get("field"), column));
        }
        return createQuery(criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])))).getResultList();
    }
}
