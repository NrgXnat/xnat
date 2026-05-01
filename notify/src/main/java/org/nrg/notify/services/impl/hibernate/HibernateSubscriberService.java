/*
 * notify: org.nrg.notify.services.impl.hibernate.HibernateSubscriberService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.services.impl.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.notify.daos.SubscriberDAO;
import org.nrg.notify.entities.Subscriber;
import org.nrg.notify.services.SubscriberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class HibernateSubscriberService extends AbstractHibernateEntityService<Subscriber, SubscriberDAO> implements SubscriberService {
    /**
     * Creates a new {@link Subscriber subscriber} objects with the submitted attributes.
     *
     * @param name   The username.
     * @param emails All email addresses associated with the subscriber. If more than one email is
     *               provided, the addresses should be separated by commas (whitespace is OK).
     *
     * @return A {@link Subscriber subscriber} object with the submitted attributes.
     *
     * @see SubscriberService#createSubscriber(String, String)
     */
    @Override
    @Transactional
    public Subscriber createSubscriber(String name, String emails) {
        // TODO: Check for subscriber with existing name.
        log.debug("Creating a new subscriber, name: {}, emails: {}", name, emails);
        Subscriber subscriber = newEntity();
        subscriber.setName(name);
        subscriber.setEmails(emails);
        getDao().create(subscriber);
        return subscriber;
    }

    /**
     * Gets the requested subscriber.
     *
     * @param name The name of the subscriber.
     *
     * @return The requested subscriber if found, <b>null</b> otherwise.
     */
    @Override
    @Transactional
    public Subscriber getSubscriberByName(String name) {
        log.debug("Looking for subscriber with name: {}", name);
        return getDao().getSubscriberByName(name);
    }
}
