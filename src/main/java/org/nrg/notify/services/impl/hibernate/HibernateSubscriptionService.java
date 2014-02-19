/**
 * HibernateSubscriptionService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services.impl.hibernate;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.notify.daos.SubscriptionDAO;
import org.nrg.notify.entities.Definition;
import org.nrg.notify.entities.Subscriber;
import org.nrg.notify.entities.Subscription;
import org.nrg.notify.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class HibernateSubscriptionService extends AbstractHibernateEntityService<Subscription> implements SubscriptionService {

    /**
     * Finds all subscriptions for the indicated {@link Definition definition}.
     * @param subscriber The {@link Subscriber subscriber}.
     * @return A list of subscriptions for the {@link Definition}.
     * @see SubscriptionService#getSubscriptionsForDefinition(Definition)
     */
    @Override
    @Transactional
    public List<Subscription> getSubscriptionsForDefinition(Definition definition) {
        return getDao().getSubscriptionsForDefinition(definition);
    }

    /**
     * Gets the {@link Subscription subscriptions} for the indicated {@link Definition definition}, then maps them by 
     * {@link Subscriber#getName() subscriber name}.
     * @see SubscriptionService#getSubscriberMapOfSubscriptionsForDefinition(Definition)
     */
    @Override
    @Transactional
    public Map<Subscriber, Subscription> getSubscriberMapOfSubscriptionsForDefinition(Definition definition) {
        List<Subscription> subscriptions = getDao().getSubscriptionsForDefinition(definition);
        Map<Subscriber, Subscription> map = new Hashtable<Subscriber, Subscription>();
        for (Subscription subscription : subscriptions) {
            map.put(subscription.getSubscriber(), subscription);
        }
        return map;
    }

    /**
     * @return A new empty {@link Subscription} object.
     * @see SubscriptionService#newEntity()
     */
    public Subscription newEntity() {
        _log.debug("Creating a new subscription object");
        return new Subscription();
    }

    /**
     * @return
     * @see AbstractHibernateEntityService#getDao()
     */
    @Override
    protected SubscriptionDAO getDao() {
        return _dao;
    }

    private static final Log _log = LogFactory.getLog(HibernateSubscriptionService.class);

    @Autowired
    private SubscriptionDAO _dao;
}
