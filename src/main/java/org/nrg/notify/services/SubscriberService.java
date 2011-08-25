/**
 * SubscriberService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 25, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.notify.api.Subscriber;

/**
 * 
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public interface SubscriberService extends BaseHibernateService<Subscriber> {
    public static String SERVICE_NAME = "SubscriberService";
}
