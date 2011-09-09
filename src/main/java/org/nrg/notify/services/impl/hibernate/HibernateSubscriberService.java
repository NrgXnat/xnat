/**
 * HibernateSubscriberService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services.impl.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.notify.daos.SubscriberDAO;
import org.nrg.notify.entities.Subscriber;
import org.nrg.notify.exceptions.DuplicateSubscriberException;
import org.nrg.notify.services.SubscriberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Implements the {@link SubscriberService} interface to provide default {@link Subscriber subscriber}
 * management functionality.
 * 
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
@Service
public class HibernateSubscriberService extends AbstractHibernateEntityService<Subscriber> implements SubscriberService {

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
        return getDao().getSubscriberByName(name);
    }
    
    /**
     * @return A new empty {@link Subscriber} object.
     * @see SubscriberService#newEntity()
     */
    public Subscriber newEntity() {
        _log.debug("Creating a new subscriber object");
        return new Subscriber();
    }

    /**
     * @return
     * @see AbstractHibernateEntityService#getDao()
     */
    @Override
    protected SubscriberDAO getDao() {
        return _dao;
    }

    private static final Log _log = LogFactory.getLog(HibernateSubscriberService.class);

    @Autowired
    private SubscriberDAO _dao;
}
