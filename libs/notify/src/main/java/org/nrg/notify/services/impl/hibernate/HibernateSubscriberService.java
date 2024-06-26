/*
 * notify: org.nrg.notify.services.impl.hibernate.HibernateSubscriberService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.services.impl.hibernate;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.notify.daos.SubscriberDAO;
import org.nrg.notify.entities.Subscriber;
import org.nrg.notify.exceptions.DuplicateSubscriberException;
import org.nrg.notify.services.SubscriberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HibernateSubscriberService extends AbstractHibernateEntityService<Subscriber, SubscriberDAO> implements SubscriberService {

    /**
     * Creates a new {@link Subscriber subscriber} objects with the submitted attributes.
     * @param name The user name.
     * @param emails All email addresses associated with the subscriber. If more than one email is
     *               provided, the addresses should be separated by commas (whitespace is OK).
     * @return A {@link Subscriber subscriber} object with the submitted attributes.
     * @see SubscriberService#createSubscriber(String, String)
     * @throws DuplicateSubscriberException When a subscriber with the same username already exists.
     */
    @Override
    @Transactional
    public Subscriber createSubscriber(String name, String emails) throws DuplicateSubscriberException {
        // TODO: Check for subscriber with existing name.
        if (_log.isDebugEnabled()) {
            _log.debug("Creating a new subscriber, name: " + name + ", emails: " + emails);
        }
        Subscriber subscriber = newEntity();
        subscriber.setName(name);
        subscriber.setEmails(emails);
        getDao().create(subscriber);
        return subscriber;
    }

    /**
     * Gets the requested subscriber.
     * @param name The name of the subscriber.
     * @return The requested subscriber if found, <b>null</b> otherwise.
     */
    @Override
    @Transactional
    public Subscriber getSubscriberByName(String name) {
        if (_log.isDebugEnabled()) {
            _log.debug("Looking for subscriber with name: " + name);
        }
        return getDao().getSubscriberByName(name);
    }

    private static final Logger _log = LoggerFactory.getLogger(HibernateSubscriberService.class);
}
