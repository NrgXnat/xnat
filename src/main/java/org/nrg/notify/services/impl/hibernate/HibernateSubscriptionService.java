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

import java.util.List;

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


/**
 * Implements the {@link SubscriptionService} interface to provide default {@link Subscription subscription}
 * management functionality.
 * 
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
@Service
public class HibernateSubscriptionService extends AbstractHibernateEntityService<Subscription> implements SubscriptionService {

    /**
     * Finds all subscriptions for the indicated {@link Definition definition}.
     * @param subscriber The {@link Subscriber subscriber}.
     * @return A list of subscriptions for the {@link Definition}.
     * @see SubscriptionService#getSubscriptionsForDefinition(Definition)
     */
    @Override
    public List<Subscription> getSubscriptionsForDefinition(Definition definition) {
        return getDao().getSubscriptionsForDefinition(definition);
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
