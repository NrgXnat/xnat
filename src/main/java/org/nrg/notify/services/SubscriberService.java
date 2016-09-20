/*
 * org.nrg.notify.services.SubscriberService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.notify.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.notify.entities.Subscriber;
import org.nrg.notify.exceptions.DuplicateSubscriberException;

public interface SubscriberService extends BaseHibernateService<Subscriber> {
    @SuppressWarnings("unused")
    public static String SERVICE_NAME = "SubscriberService";
    
    /**
     * Creates a new {@link Subscriber subscriber} objects with the submitted attributes.
     * @param name The user name.
     * @param emails All email addresses associated with the subscriber. If more than one email is
     *               provided, the addresses should be separated by commas (whitespace is OK).
     * @return A {@link Subscriber subscriber} object with the submitted attributes.
     * @throws DuplicateSubscriberException When a subscriber with the same username already exists.
     */
    abstract public Subscriber createSubscriber(String name, String emails) throws DuplicateSubscriberException;

    /**
     * Gets the requested subscriber.
     * @param name The name of the subscriber.
     * @return The requested subscriber if found, <b>null</b> otherwise.
     */
    abstract public Subscriber getSubscriberByName(String name);
}
