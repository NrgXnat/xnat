/**
 * SubscriberDAO
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
import org.nrg.notify.entities.Subscriber;
import org.springframework.stereotype.Repository;


/**
 * Implements the DAO class for the {@link Subscriber} entity type.
 * 
 * @see AbstractHibernateDAO
 * @author Rick Herrick <rick.herrick@wustl.edu>

 */
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
