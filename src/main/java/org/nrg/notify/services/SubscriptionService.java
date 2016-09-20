/*
 * org.nrg.notify.services.SubscriptionService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.notify.services;

import java.util.List;
import java.util.Map;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.notify.entities.Definition;
import org.nrg.notify.entities.Subscriber;
import org.nrg.notify.entities.Subscription;

public interface SubscriptionService extends BaseHibernateService<Subscription> {
    public static String SERVICE_NAME = "SubscriptionService";

    /**
     * Returns all subscriptions for the indicated definition.
     * @param definition    The definition for which you wish to retrieve definitions.
     * @return A list of the subscriptions for the indicated definition.
     */
    abstract public List<Subscription> getSubscriptionsForDefinition(Definition definition);

    /**
     * Returns all subscriptions for the indicated definition, sorted into a map keyed by the subscriber object.
     * @param definition    The definition for which you wish to retrieve definitions.
     * @return A list of the subscriptions for the indicated definition.
     */
    abstract public Map<Subscriber, Subscription> getSubscriberMapOfSubscriptionsForDefinition(Definition definition);
}
