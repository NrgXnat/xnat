/**
 * SubscriberService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.notify.entities.Subscriber;


/**
 * 
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public interface SubscriberService extends BaseHibernateService<Subscriber> {
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
}
