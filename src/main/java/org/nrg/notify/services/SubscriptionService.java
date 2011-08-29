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

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.notify.entities.Subscription;


/**
 * 
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public interface SubscriptionService extends BaseHibernateService<Subscription> {
    public static String SERVICE_NAME = "SubscriptionService";
}
