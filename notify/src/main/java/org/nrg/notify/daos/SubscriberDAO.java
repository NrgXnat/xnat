/*
 * notify: org.nrg.notify.daos.SubscriberDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.daos;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.notify.entities.Subscriber;
import org.springframework.stereotype.Repository;

@Repository
public class SubscriberDAO extends AbstractHibernateDAO<Subscriber> {
    /**
     * Gets the requested subscriber.
     *
     * @param name The name of the subscriber.
     *
     * @return The requested subscriber if found, <b>null</b> otherwise.
     */
    public Subscriber getSubscriberByName(String name) {
        return findByUniqueProperty("name", name);
    }
}
