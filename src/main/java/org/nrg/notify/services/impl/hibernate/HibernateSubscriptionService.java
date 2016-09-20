/*
 * org.nrg.notify.services.impl.hibernate.HibernateSubscriptionService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.notify.services.impl.hibernate;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.notify.daos.SubscriptionDAO;
import org.nrg.notify.entities.Definition;
import org.nrg.notify.entities.Subscriber;
import org.nrg.notify.entities.Subscription;
import org.nrg.notify.services.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HibernateSubscriptionService extends AbstractHibernateEntityService<Subscription, SubscriptionDAO> implements SubscriptionService {

    /**
     * Finds all subscriptions for the indicated {@link Definition definition}.
     * @param definition    The {@link Definition definition}.
     * @return A list of subscriptions for the {@link Definition}.
     * @see SubscriptionService#getSubscriptionsForDefinition(Definition)
     */
    @Override
    @Transactional
    public List<Subscription> getSubscriptionsForDefinition(Definition definition) {
        if (_log.isDebugEnabled()) {
            _log.debug("Looking for subscriptions for definition: " + definition.toString());
        }

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
        Map<Subscriber, Subscription> map = new Hashtable<>();
        for (Subscription subscription : subscriptions) {
            map.put(subscription.getSubscriber(), subscription);
        }
        if (_log.isDebugEnabled()) {
            _log.debug("Found " + map.size() + " subscribers for definition: " + definition.toString());
        }
        return map;
    }

    private static final Logger _log = LoggerFactory.getLogger(HibernateSubscriptionService.class);
}
