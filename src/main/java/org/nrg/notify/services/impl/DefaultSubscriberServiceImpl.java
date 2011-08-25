/**
 * DefaultCategoryServiceImpl
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 24, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.notify.api.Subscriber;
import org.nrg.notify.daos.SubscriberDAO;
import org.nrg.notify.services.SubscriberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implements the {@link SubscriberService} interface to provide default {@link Subscriber subscriber}
 * management functionality.
 * 
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
@Service
public class DefaultSubscriberServiceImpl extends AbstractHibernateEntityService<Subscriber> implements SubscriberService {

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

    private static final Log _log = LogFactory.getLog(DefaultSubscriberServiceImpl.class);

    @Autowired
    private SubscriberDAO _dao;
}
