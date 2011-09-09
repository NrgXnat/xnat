/**
 * SubscriptionService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services;

import java.util.List;
import java.util.Map;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.notify.entities.Definition;
import org.nrg.notify.entities.Subscriber;
import org.nrg.notify.entities.Subscription;

/**
 * Manages subscriptions and subscription queries.
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public interface SubscriptionService extends BaseHibernateService<Subscription> {
    public static String SERVICE_NAME = "SubscriptionService";

    /**
     * @param definition
     * @return
     */
    abstract public List<Subscription> getSubscriptionsForDefinition(Definition definition);

    /**
     * @param definition
     * @return
     */
    abstract public Map<Subscriber, Subscription> getSubscriberMapOfSubscriptionsForDefinition(Definition definition);
}
