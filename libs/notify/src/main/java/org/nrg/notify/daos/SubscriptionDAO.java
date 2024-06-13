/*
 * notify: org.nrg.notify.daos.SubscriptionDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.notify.entities.Definition;
import org.nrg.notify.entities.Subscription;
import org.springframework.stereotype.Repository;


@Repository
public class SubscriptionDAO extends AbstractHibernateDAO<Subscription> {

    /**
     * Returns all of the subscriptions for a particular {@link Definition definition}.
     * @param definition    The definition on which to search.
     * @return The subscriptions for the indicated definition.
     */
    @SuppressWarnings("unchecked")
    public List<Subscription> getSubscriptionsForDefinition(Definition definition) {
        Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("definition", definition));
        criteria.add(Restrictions.eq("enabled", true));
        return criteria.list();
    }
}
