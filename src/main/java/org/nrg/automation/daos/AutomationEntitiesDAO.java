package org.nrg.automation.daos;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.framework.orm.hibernate.BaseHibernateEntity;

import java.io.Serializable;

public class AutomationEntitiesDAO<E extends BaseHibernateEntity> extends AbstractHibernateDAO<E> {
    protected void addProjectRestrictions(final Criteria criteria, final String projectId, final boolean exactMatchExternalId) {
        addProjectRestrictions(criteria, projectId, exactMatchExternalId, "externalId");
    }

    protected void addProjectRestrictions(final Criteria criteria, final String projectId, final boolean exactMatchExternalId, final String restrictionName) {
        if (StringUtils.isNotBlank(projectId)) {
            final Criterion extId1 = Restrictions.eq(restrictionName, projectId);
            final Criterion extId2 = Restrictions.like(restrictionName, projectId + "_", MatchMode.START);
            if (exactMatchExternalId) {
                criteria.add(extId1);
            } else {
                criteria.add(Restrictions.or(extId1, extId2));
            }
        }
    }
}
