/*
 * notify: org.nrg.notify.daos.SubscriberDAO
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
import org.nrg.notify.entities.Subscriber;
import org.springframework.stereotype.Repository;


@Repository
public class SubscriberDAO extends AbstractHibernateDAO<Subscriber> {

    /**
     * Gets the requested subscriber.
     * @param name The name of the subscriber.
     * @return The requested subscriber if found, <b>null</b> otherwise.
     */
    public Subscriber getSubscriberByName(String name) {
        Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("name", name));
        criteria.add(Restrictions.eq("enabled", true));
        @SuppressWarnings("unchecked")
        List<Subscriber> subscribers = criteria.list();
        
        if (subscribers == null || subscribers.size() == 0) {
            return null;
        }
        
        return subscribers.get(0);
    }
}
