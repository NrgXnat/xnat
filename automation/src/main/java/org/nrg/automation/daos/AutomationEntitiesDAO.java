/*
 * automation: org.nrg.automation.daos.AutomationEntitiesDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.daos;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.framework.orm.hibernate.BaseHibernateEntity;
import org.nrg.framework.orm.hibernate.QueryBuilder;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

@Slf4j
public abstract class AutomationEntitiesDAO<E extends BaseHibernateEntity> extends AbstractHibernateDAO<E> {
    protected Predicate getProjectRestriction(final QueryBuilder<E> builder, final String projectId, final boolean exactMatchExternalId) {
        return getProjectRestriction(builder, projectId, exactMatchExternalId, "externalId");
    }

    protected Predicate getProjectRestriction(final QueryBuilder<E> builder, final String projectId, final boolean exactMatchExternalId, final String restrictionName) {
        Predicate p1 = builder.eq(restrictionName, projectId);
        Predicate p2 = builder.like(restrictionName, projectId + "_%");
        return exactMatchExternalId ? p1 : builder.or(p1, p2);
    }

    protected <T extends BaseHibernateEntity> Predicate getProjectRestriction(final CriteriaBuilder builder, final From<E, T> from, final String projectId, final boolean exactMatchExternalId) {
        return getProjectRestriction(builder, from, projectId, exactMatchExternalId, "externalId");
    }

    protected <T extends BaseHibernateEntity> Predicate getProjectRestriction(final CriteriaBuilder builder, final From<E, T> from, final String projectId, final boolean exactMatchExternalId, final String restrictionName) {
        Predicate p1 = builder.equal(from.get(restrictionName), projectId);
        Predicate p2 = builder.like(from.get(restrictionName), projectId + "_%");
        return exactMatchExternalId ? p1 : builder.or(p1, p2);
    }
}
