/*
 * automation: org.nrg.automation.daos.PersistentEventDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.daos;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.automation.event.entities.PersistentEvent;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Query;

/**
 * The Class PersistentEventDAO.
 */
@Repository
@Slf4j
public class PersistentEventDAO extends AbstractHibernateDAO<PersistentEvent> {
    private static final String QUERY_DISTINCT_VALUES = "select distinct c.%s from %s c where c.externalId = :projectId";

    /**
     * Gets the distinct values.
     *
     * @param clazz     the clazz
     * @param column    the column
     * @param projectId the project id
     *
     * @return the distinct values
     */
    public List<Object[]> getDistinctValues(Class<AutomationEventImplementerI> clazz, String column, String projectId) {
        Query query = createQuery(QUERY_DISTINCT_VALUES.formatted(column, clazz.getName())).setParameter("projectId", projectId);
        //noinspection unchecked
        return (List<Object[]>) query.getResultList();
    }

}
