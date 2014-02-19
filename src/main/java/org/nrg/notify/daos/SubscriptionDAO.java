/**
 * SubscriptionDAO
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
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
